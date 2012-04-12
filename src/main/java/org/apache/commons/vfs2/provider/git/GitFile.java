package org.apache.commons.vfs2.provider.git;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitFile extends AbstractFileObject implements FileObject
{

	private final FileName rootName;
	private final GitFileSystem gitFileSystem;

	public GitFile(GitFileSystem gitFileSystem, FileName rootName, AbstractFileName name)
	{
		super(name, gitFileSystem);
		this.rootName = rootName;
		this.gitFileSystem = gitFileSystem;

	}

	@Override
	protected FileType doGetType() throws Exception
	{
		if (isRootDir())
		{
			return FileType.FOLDER;
		}

		Repository repository = gitFileSystem.getRepository();
		RevTree tree = gitFileSystem.getTree();

		TreeWalk treeWalk = buildTreeWalk(repository, tree);
		if (treeWalk == null)
		{
			// The file isn't in the local repository
			return FileType.IMAGINARY;
		} else
		{
			FileMode fileMode = treeWalk.getFileMode(0);
			if (fileMode == FileMode.TREE)
			{
				return FileType.FOLDER;
			} else if (fileMode == FileMode.EXECUTABLE_FILE || fileMode == FileMode.REGULAR_FILE)
			{
				return FileType.FILE;
			} else
			{
				return FileType.IMAGINARY;
			}

		}

	}

	@Override
	protected String[] doListChildren() throws Exception
	{
		Repository repository = gitFileSystem.getRepository();
		RevTree tree = gitFileSystem.getTree();
		TreeWalk treeWalk = buildTreeWalk(repository, tree);

		treeWalk.enterSubtree();
		List<String> results = new ArrayList<String>();

		while (treeWalk.next())
		{
			String strName = treeWalk.getNameString();
			results.add(strName);
		}

		return UriParser.encode(results.toArray(new String[results.size()]));
	}

	private TreeWalk buildTreeWalk(Repository repository, RevTree tree) throws IOException
	{
		// The treeWalker doesn't work with "." path so consider it as
		// particular path
		TreeWalk treeWalk = null;
		if (isRootDir())
		{
			treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
		} else
		{
			treeWalk = TreeWalk.forPath(repository, rootName.getRelativeName(getName()), tree);
		}
		return treeWalk;
	}

	private boolean isRootDir()
	{
		return getName().compareTo(rootName) == 0;
	}

	@Override
	protected long doGetContentSize() throws Exception
	{
		Repository repository = gitFileSystem.getRepository();
		RevTree tree = gitFileSystem.getTree();

		TreeWalk treeWalk = buildTreeWalk(repository, tree);
		ObjectLoader objectLoader = repository.open(treeWalk.getObjectId(0));
		return objectLoader.getSize();
	}

	@Override
	protected InputStream doGetInputStream() throws Exception
	{
		Repository repository = gitFileSystem.getRepository();
		RevTree tree = gitFileSystem.getTree();

		TreeWalk treeWalk = buildTreeWalk(repository, tree);
		ObjectLoader objectLoader = repository.open(treeWalk.getObjectId(0));
		return objectLoader.openStream();
	}
}
