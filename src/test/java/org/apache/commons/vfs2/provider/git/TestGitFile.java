package org.apache.commons.vfs2.provider.git;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class TestGitFile
{

	private Git git;
	private FileObject rootFile;
	private File workTree;

	@Before
	public void createGitRepository() throws IOException, NoFilepatternException, NoHeadException, NoMessageException, ConcurrentRefUpdateException, WrongRepositoryStateException
	{
		File tmpFile = File.createTempFile("git", "test");
		tmpFile.delete();
		tmpFile.mkdirs();

		workTree = tmpFile;

		File gitDir = new File(workTree, Constants.DOT_GIT);

		Repository repo = new RepositoryBuilder().setGitDir(gitDir).setWorkTree(workTree).build();
		repo.create();

		git = new Git(repo);
		createAndCommitFile(".placeholder");

		StandardFileSystemManager manager = new StandardFileSystemManager();
		manager.addProvider("git", new GitProvider());
		manager.setCacheStrategy(CacheStrategy.ON_CALL);
		manager.setFilesCache(new SoftRefFilesCache());

		FileSystemOptions options = new FileSystemOptions();
		File gitDirectory = git.getRepository().getDirectory();
		GitFileSystemConfigBuilder.getInstance().setGitDirectory(options, gitDirectory);

		rootFile = manager.resolveFile("git:/.", options);
	}

	@After
	public void destroyGitRepository() throws IOException
	{
		rootFile = null;
		git.getRepository().close();
		FileUtils.deleteDirectory(workTree);
	}

	@Test
	public void testRootGetName()
	{
		assertThat(rootFile.getName().getPath()).isEqualTo("/");
	}

	@Test
	public void testGetName() throws NoFilepatternException, NoHeadException, NoMessageException, ConcurrentRefUpdateException, JGitInternalException,
			WrongRepositoryStateException, IOException
	{
		createAndCommitFile("file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/file.txt");
		assertThat(resolvedFile.getName().getPath()).isEqualTo("/file.txt");

	}

	@Test
	public void testExistsOnCommittedFile() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndCommitFile("file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/file.txt");
		assertThat(resolvedFile.exists()).isTrue();
	}

	@Test
	public void testExistsOnNotCommittedFile() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndNotCommitFile("file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/file.txt");
		assertThat(resolvedFile.exists()).isFalse();
	}

	@Test
	public void testExistsOnCommittedFolder() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndCommitFile("folder/file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/folder");
		assertThat(resolvedFile.exists()).isTrue();
	}

	@Test
	public void testExistsOnNotCommittedFolder() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndNotCommitFile("folder/file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/folder");
		assertThat(resolvedFile.exists()).isFalse();
	}

	@Test
	public void testGetTypeOfRootDirectory() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{

		assertThat(rootFile.getType()).isEqualTo(FileType.FOLDER);
	}

	@Test
	public void testGetTypeOnCommittedFile() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndCommitFile("file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/file.txt");
		assertThat(resolvedFile.getType()).isEqualTo(FileType.FILE);
	}

	@Test
	public void testGetTypeOnNotCommittedFile() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndNotCommitFile("file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/file.txt");
		assertThat(resolvedFile.getType()).isEqualTo(FileType.IMAGINARY);
	}

	@Test
	public void testGetTypeOnCommittedFolder() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndCommitFile("folder/file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/folder");
		assertThat(resolvedFile.getType()).isEqualTo(FileType.FOLDER);
	}

	@Test
	public void testGetTypeOnNotCommittedFolder() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		createAndNotCommitFile("folder/file.txt");

		FileObject resolvedFile = rootFile.resolveFile("/folder");
		assertThat(resolvedFile.getType()).isEqualTo(FileType.IMAGINARY);
	}

	@Test
	public void testGetContent() throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException, IOException
	{
		String relativePath = "folder/subfolder/filewithcontent.txt";
		final int contentSize = 150;
		byte[] contents = new byte[contentSize];
		new Random(System.currentTimeMillis()).nextBytes(contents);
		createAndCommitFile(relativePath, contents);
		
		FileObject resolvedFile = rootFile.resolveFile(relativePath);
		FileContent content = resolvedFile.getContent();
		
		assertThat(content.getSize()).isEqualTo(contentSize);
		byte[] result = new byte[contentSize];
		IOUtils.readFully(content.getInputStream(), result);
		
		assertThat(result).isEqualTo(contents);

	}

	private void createAndCommitFile(String relativePath, byte[] contents) throws IOException, NoFilepatternException, NoHeadException, NoMessageException,
			ConcurrentRefUpdateException, WrongRepositoryStateException
	{
		File file = createAndNotCommitFile(relativePath);
		Files.write(contents, file);
		commitFile(relativePath);
	}

	private File createAndNotCommitFile(String relativePath) throws IOException
	{
		File absoluteFile = new File(workTree, relativePath);

		absoluteFile.getParentFile().mkdirs();
		Files.touch(absoluteFile);

		return absoluteFile;
	}

	private void createAndCommitFile(String relativePath) throws IOException, NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException,
			ConcurrentRefUpdateException, WrongRepositoryStateException
	{
		createAndNotCommitFile(relativePath);

		commitFile(relativePath);
	}

	private void commitFile(String relativePath) throws NoFilepatternException, NoHeadException, NoMessageException, UnmergedPathException, ConcurrentRefUpdateException,
			WrongRepositoryStateException
	{
		git.add().addFilepattern(relativePath).call();
		git.commit().setMessage("msg").call();
	}

	@Test
	public void testGetFileSystem()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetURL()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIsHidden()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIsReadable()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIsWriteable()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetParent()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetChildren()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetChild()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testResolveFileStringNameScope()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testResolveFileString()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDelete()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDeleteFileSelector()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateFile()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateFolder()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCopyFrom()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testMoveTo()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCanRenameTo()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testFindFilesFileSelector()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testRefresh()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testClose()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetInputStream()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetRandomAccessContent()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetOutputStream()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetOutputStreamBoolean()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testFindFilesFileSelectorBooleanListOfFileObject()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIsContentOpen()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testIsAttached()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testHoldObject()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetFileOperations()
	{
		fail("Not yet implemented"); // TODO
	}

}
