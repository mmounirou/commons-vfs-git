package org.apache.commons.vfs2.provider.local;

import java.io.File;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.util.RandomAccessMode;

public class GitFileRandomAccessContent extends LocalFileRandomAccessContent
{

	public GitFileRandomAccessContent(File localFile, RandomAccessMode mode) throws FileSystemException
	{
		super(localFile, mode);
	}

}