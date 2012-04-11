package org.apache.commons.vfs2.provider.git;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: mmounirou
 * Date: 11/04/12
 * Time: 19:54
 * To change this template use File | Settings | File Templates.
 */
public class GitFile extends AbstractFileObject {

    public GitFile(AbstractFileName name, GitFileSystem gitFileSystem, FileName rootName)
    {
        super(name,gitFileSystem);
    }

    @Override
    protected FileType doGetType() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected String[] doListChildren() throws Exception {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected long doGetContentSize() throws Exception {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
