package org.apache.commons.vfs2.provider.git;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: mmounirou
 * Date: 11/04/12
 * Time: 19:48
 * To change this template use File | Settings | File Templates.
 */
public class GitFileSystem extends AbstractFileSystem {

    private FileSystemOptions options;
    private GenericFileName rootName;

    public GitFileSystem(GenericFileName name, FileSystemOptions fileSystemOptions)
    {
        super(name,null,fileSystemOptions);
    }

    @Override
    protected FileObject createFile(AbstractFileName name) throws Exception {
        return new GitFile(name,this,getRootName());
    }

    @Override
    protected void addCapabilities(Collection<Capability> caps) {
        caps.addAll(GitProvider.CAPABILITIES);
    }
}
