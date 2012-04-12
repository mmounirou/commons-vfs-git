package org.apache.commons.vfs2.provider.git;

import java.io.File;
import java.util.Date;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

public class GitFileSystemConfigBuilder extends FileSystemConfigBuilder
{

	private static final GitFileSystemConfigBuilder BUILDER = new GitFileSystemConfigBuilder();

	private static final String GIT_DIR = GitFileSystemConfigBuilder.class.getName() + ".GIT_DIR";
	private static final String TREE_DIR = GitFileSystemConfigBuilder.class.getName() + ".TREE_DIR";
	private static final String COMMIT_DATE = GitFileSystemConfigBuilder.class.getName() + ".COMMIT_DATE";
	private static final String COMMIT_REF = GitFileSystemConfigBuilder.class.getName() + ".COMMIT_REF";
	private static final String COMMIT_REV = GitFileSystemConfigBuilder.class.getName() + ".COMMIT_REV";
	private static final String REF = GitFileSystemConfigBuilder.class.getName() + ".REF";

	private GitFileSystemConfigBuilder()
	{
		super("git.");
	}

	@Override
	protected Class<? extends FileSystem> getConfigClass()
	{
		return GitFileSystem.class;
	}

	public static GitFileSystemConfigBuilder getInstance()
	{
		return BUILDER;
	}

	public File getGitDirectory(FileSystemOptions opts)
	{
		return (File) getParam(opts, GIT_DIR);
	}

	public void setGitDirectory(FileSystemOptions opts, File gitDirectory)
	{
		setParam(opts, GIT_DIR, gitDirectory);
	}

	public File getTreeDirectory(FileSystemOptions opts)
	{
		return (File) getParam(opts, TREE_DIR);
	}

	public void setTreeDirectory(FileSystemOptions opts, File treeDirectory)
	{
		setParam(opts, TREE_DIR, treeDirectory);
	}

	public Date getCommitDate(FileSystemOptions opts)
	{
		return (Date) getParam(opts, COMMIT_DATE);
	}

	public void setCommitDate(FileSystemOptions opts, Date commitDate)
	{
		setParam(opts, COMMIT_DATE, commitDate);
	}

	public String getCommitReference(FileSystemOptions opts)
	{
		return getString(opts, COMMIT_REF);
	}

	public void setCommitReference(FileSystemOptions opts, String commitReference)
	{
		setParam(opts, COMMIT_REF, commitReference);
	}

	public String getReference(FileSystemOptions opts)
	{
		return getString(opts, REF);
	}

	public void setReference(FileSystemOptions opts, String reference)
	{
		setParam(opts, REF, reference);
	}

	public int getCommitRevision(FileSystemOptions opts)
	{
		return getInteger(opts, COMMIT_REV);
	}

	public void setCommitRevision(FileSystemOptions opts, int commitRevision)
	{
		setParam(opts, COMMIT_REV, commitRevision);
	}

}
