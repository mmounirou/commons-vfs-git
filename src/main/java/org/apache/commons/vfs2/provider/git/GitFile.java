package org.apache.commons.vfs2.provider.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.git.utils.GitFileOutputStream;
import org.apache.commons.vfs2.provider.local.GitFileRandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitFile extends AbstractFileObject implements FileObject
{

	private final FileName rootName;

	public GitFile(GitFileSystem gitFileSystem, FileName rootName, AbstractFileName name)
	{
		super(name, gitFileSystem);
		this.rootName = getGitRootDir(name);
	}

	private FileName getGitRootDir(AbstractFileName name)
	{
		try
		{
			Repository repository = getGitFileSystem().getRepository(name);
			return getRootName(name, repository);
		}
		catch ( IOException e )
		{
			// TODO throw new FileSystemException(e);
			return null;
		}
	}

	private FileName getRootName(AbstractFileName name, Repository repository) throws FileSystemException
	{
		File rootDirectory = repository.getDirectory().getParentFile();
		FileName rootDirName = VFS.getManager().resolveFile(rootDirectory, "").getName();
		FileName rootName = name;

		while ( rootName.getParent() != name )
		{
			if ( rootName.getPath().endsWith(rootDirName.getPath()) )
			{
				return rootName;
			}
			rootName = rootName.getParent();
		}

		// TODO throw an assertion
		return null;
	}

	@Override
	protected FileType doGetType() throws Exception
	{
		if ( isRootDir() )
		{
			return FileType.FOLDER;
		}

		Repository repository = getGitFileSystem().getRepository(getName());
		RevTree tree = getGitFileSystem().getTree(getName());

		TreeWalk treeWalk = buildTreeWalk(repository, tree);
		if ( treeWalk == null )
		{
			// The file isn't in the local repository
			return FileType.IMAGINARY;
		}
		else
		{
			FileMode fileMode = treeWalk.getFileMode(0);
			if ( fileMode == FileMode.TREE )
			{
				return FileType.FOLDER;
			}
			else if ( fileMode == FileMode.EXECUTABLE_FILE || fileMode == FileMode.REGULAR_FILE )
			{
				return FileType.FILE;
			}
			else
			{
				return FileType.IMAGINARY;
			}

		}

	}

	@Override
	protected String[] doListChildren() throws Exception
	{
		Repository repository = getGitFileSystem().getRepository(getName());
		RevTree tree = getGitFileSystem().getTree(getName());
		TreeWalk treeWalk = buildTreeWalk(repository, tree);

		treeWalk.enterSubtree();
		List<String> results = new ArrayList<String>();

		while ( treeWalk.next() )
		{
			String strName = treeWalk.getNameString();
			results.add(strName);
		}

		return UriParser.encode(results.toArray(new String[results.size()]));
	}

	@Override
	protected void doDelete() throws Exception
	{
		Repository repository = getGitFileSystem().getRepository(getName());
		Git git = new Git(repository);
		git.rm().addFilepattern(getRelativePath()).call();
		git.commit().setMessage(String.format("Delete %s", getRelativePath()));
	}

	@Override
	protected void doRename(FileObject newfile) throws Exception
	{
		Repository repository = getGitFileSystem().getRepository(getName());
		Git git = new Git(repository);

		// remove old file
		git.rm().addFilepattern(getRelativePath()).call();

		// rename on file system
		new File(getName().getPathDecoded()).renameTo(new File(newfile.getName().getPathDecoded()));

		// add new file
		git.add().addFilepattern(rootName.getRelativeName(newfile.getName()));

		git.commit().setMessage(String.format("Rename %s to %s", getRelativePath(), rootName.getRelativeName(newfile.getName())));

	}

	@Override
	protected void doCreateFolder() throws Exception
	{
		new File(getName().getPathDecoded()).mkdir();
	}

	@Override
	protected long doGetLastModifiedTime() throws Exception
	{
		// TODO modified to return the date of last commit which contains this
		// file
		return new File(getName().getPathDecoded()).lastModified();
	}

	@Override
	protected boolean doSetLastModifiedTime(long modtime) throws Exception
	{
		// TODO modified to support only if the modtime is now.
		return new File(getName().getPathDecoded()).setLastModified(modtime);
	}

	@Override
	protected RandomAccessContent doGetRandomAccessContent(RandomAccessMode mode) throws Exception
	{

		// TODO find a way to not copy the content fully for random access

		File tempFile = File.createTempFile("plpo", "plpo");
		tempFile.deleteOnExit();

		InputStream inputStream = doGetInputStream();
		OutputStream outPutStream = new FileOutputStream(tempFile);

		try
		{
			IOUtils.copyLarge(inputStream, outPutStream);
		}
		finally
		{
			IOUtils.closeQuietly(outPutStream);
			IOUtils.closeQuietly(inputStream);
		}

		return new GitFileRandomAccessContent(tempFile, mode);
	}

	@Override
	protected OutputStream doGetOutputStream(boolean bAppend) throws Exception
	{
		return new GitFileOutputStream(getName().getPathDecoded(), bAppend, this);
	}

	@Override
	protected long doGetContentSize() throws Exception
	{
		Repository repository = getGitFileSystem().getRepository(getName());
		RevTree tree = getGitFileSystem().getTree(getName());

		TreeWalk treeWalk = buildTreeWalk(repository, tree);
		ObjectLoader objectLoader = repository.open(treeWalk.getObjectId(0));
		return objectLoader.getSize();
	}

	@Override
	protected InputStream doGetInputStream() throws Exception
	{
		Repository repository = getGitFileSystem().getRepository(getName());
		RevTree tree = getGitFileSystem().getTree(getName());

		TreeWalk treeWalk = buildTreeWalk(repository, tree);
		ObjectLoader objectLoader = repository.open(treeWalk.getObjectId(0));
		return objectLoader.openStream();
	}

	private TreeWalk buildTreeWalk(Repository repository, RevTree tree) throws IOException
	{
		// The treeWalker doesn't work with "." path so consider it as
		// particular path
		TreeWalk treeWalk = null;
		if ( isRootDir() )
		{
			treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
		}
		else
		{
			treeWalk = TreeWalk.forPath(repository, getRelativePath(), tree);
		}
		return treeWalk;
	}

	private String getRelativePath() throws FileSystemException
	{
		return rootName.getRelativeName(getName());
	}

	private boolean isRootDir()
	{
		return getName().compareTo(rootName) == 0;
	}

	public GitFileSystem getGitFileSystem()
	{
		return (GitFileSystem) getFileSystem();
	}

	public void commit() throws IOException, GitAPIException
	{
		Repository repository = getGitFileSystem().getRepository(getName());
		Git git = new Git(repository);
		git.add().addFilepattern(getRelativePath()).call();
		git.commit().setMessage(String.format("Modify  %s", getRelativePath()));

	}
}
