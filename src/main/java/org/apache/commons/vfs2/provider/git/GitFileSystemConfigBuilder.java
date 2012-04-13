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

	public String getReference(FileSystemOptions opts)
	{
		return getString(opts, REF);
	}

	/**
	 *  * Parse a git revision string and return an object id.
	 *
	 * Combinations of these operators are supported:
	 * <ul>
	 * <li><b>HEAD</b>, <b>MERGE_HEAD</b>, <b>FETCH_HEAD</b></li>
	 * <li><b>SHA-1</b>: a complete or abbreviated SHA-1</li>
	 * <li><b>refs/...</b>: a complete reference name</li>
	 * <li><b>short-name</b>: a short reference name under {@code refs/heads},
	 * {@code refs/tags}, or {@code refs/remotes} namespace</li>
	 * <li><b>tag-NN-gABBREV</b>: output from describe, parsed by treating
	 * {@code ABBREV} as an abbreviated SHA-1.</li>
	 * <li><i>id</i><b>^</b>: first parent of commit <i>id</i>, this is the same
	 * as {@code id^1}</li>
	 * <li><i>id</i><b>^0</b>: ensure <i>id</i> is a commit</li>
	 * <li><i>id</i><b>^n</b>: n-th parent of commit <i>id</i></li>
	 * <li><i>id</i><b>~n</b>: n-th historical ancestor of <i>id</i>, by first
	 * parent. {@code id~3} is equivalent to {@code id^1^1^1} or {@code id^^^}.</li>
	 * <li><i>id</i><b>:path</b>: Lookup path under tree named by <i>id</i></li>
	 * <li><i>id</i><b>^{commit}</b>: ensure <i>id</i> is a commit</li>
	 * <li><i>id</i><b>^{tree}</b>: ensure <i>id</i> is a tree</li>
	 * <li><i>id</i><b>^{tag}</b>: ensure <i>id</i> is a tag</li>
	 * <li><i>id</i><b>^{blob}</b>: ensure <i>id</i> is a blob</li>
	 * </ul>
	 *
	 * <p>
	 * The following operators are specified by Git conventions, but are not
	 * supported by this method:
	 * <ul>
	 * <li><b>ref@{n}</b>: n-th version of ref as given by its reflog</li>
	 * <li><b>ref@{time}</b>: value of ref at the designated time</li>
	 * </ul>
	 *
	 * @param revstr
	 *            A git object references expression

	 */
	public void setReference(FileSystemOptions opts, String revstr)
	{
		setParam(opts, REF, revstr);
	}

}
