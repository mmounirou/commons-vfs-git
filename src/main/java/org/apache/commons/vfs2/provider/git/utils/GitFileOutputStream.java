package org.apache.commons.vfs2.provider.git.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.vfs2.provider.git.GitFile;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitFileOutputStream extends FileOutputStream
{

	private GitFile m_gitFile;

	public GitFileOutputStream(String pathDecoded, boolean bAppend, GitFile gitFile) throws FileNotFoundException
	{
		super(pathDecoded, bAppend);
		m_gitFile = gitFile;
	}

	@Override
	public void close() throws IOException
	{
		super.close();
		try
		{
			m_gitFile.commit();
		}
		catch ( GitAPIException e )
		{
			throw new IOException(e);
		}
	}

}
