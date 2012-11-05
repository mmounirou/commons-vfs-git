package org.apache.commons.vfs2.provider.git;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;

import com.google.common.base.Joiner;

public class Main
{
	public static void main(String[] args) throws FileSystemException
	{
		FileSystemManager fsManager = VFS.getManager();

		FileSystemOptions options = new FileSystemOptions();
		GitFileSystemConfigBuilder.getInstance().setReference(options, "aa1771a6dab53bb0a5a9e2e921259484d62e9b66");
		FileObject gitFile = fsManager.resolveFile("git:///c/Projects/Perso/commons-vfs-git/src/main/java/org/apache/commons/vfs2/provider/git", options);
		System.out.println("\n\n"+Joiner.on("\n").join(gitFile.getChildren()));
	}
}
