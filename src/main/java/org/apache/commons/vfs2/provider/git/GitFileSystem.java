package org.apache.commons.vfs2.provider.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
import org.eclipse.jgit.revwalk.DepthWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;

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
		if (repository == null)
		{
			repository = buildRepository();
		}
		return repository;
	}

	RevTree getTree() throws IOException
	{
		if (tree == null)
		{
			tree = buildTree();
		}
		return tree;
	}

	private RevTree buildTree() throws IOException
	{

		Repository repo = getRepository();
		ObjectId id = repo.resolve(repo.getFullBranch());

		DepthWalk.RevWalk walk = new DepthWalk.RevWalk(repo, 1);
		RevCommit revCommit = walk.parseCommit(id);

		return revCommit.getTree();
	}

	private Repository buildRepository() throws IOException
	{
		File gitDirectory = GitFileSystemConfigBuilder.getInstance().getGitDirectory(getFileSystemOptions());

		RepositoryBuilder builder = new RepositoryBuilder();
		return builder.setGitDir(gitDirectory).findGitDir().readEnvironment().build();
	}
}
