package org.apache.commons.vfs2.provider.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.local.LocalFileName;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;

public class GitFileSystem extends AbstractFileSystem implements FileSystem
{

	private RevTree tree;
	private Repository repository;

	public GitFileSystem(LocalFileName rootName, FileSystemOptions fileSystemOptions)
	{
		super(rootName, null, fileSystemOptions);
	}

	@Override
	protected FileObject createFile(AbstractFileName name) throws Exception
	{
		return new GitFile(this, getRootName(), name);
	}

	@Override
	protected void addCapabilities(Collection<Capability> caps)
	{
		caps.addAll(GitProvider.capabilities);
	}

	Repository getRepository() throws IOException
	{
		if ( repository == null )
		{
			repository = buildRepository();
		}
		return repository;
	}

	RevTree getTree() throws IOException
	{
		if ( tree == null )
		{
			tree = buildTree();
		}
		return tree;
	}

	private RevTree buildTree() throws IOException
	{
		Repository repo = getRepository();

		String strReference = GitFileSystemConfigBuilder.getInstance().getReference(getFileSystemOptions());
		if ( StringUtils.isBlank(strReference) )
		{
			strReference = repo.getFullBranch();
		}

		ObjectId objectId = repo.resolve(strReference);
		RevWalk walk = new RevWalk(repo);
		RevCommit revCommit = walk.parseCommit(objectId);
		return revCommit.getTree();

	}

	private Repository buildRepository() throws IOException
	{
		File gitDirectory = GitFileSystemConfigBuilder.getInstance().getGitDirectory(getFileSystemOptions());

		RepositoryBuilder builder = new RepositoryBuilder();
		return builder.setGitDir(gitDirectory).findGitDir().readEnvironment().build();
	}
}
