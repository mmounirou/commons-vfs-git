package org.apache.commons.vfs2.provider.git;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.git.GitFileSystem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mmounirou
 * Date: 11/04/12
 * Time: 19:39
 * To change this template use File | Settings | File Templates.
 */
public class GitProvider extends AbstractOriginatingFileProvider {

    static final Collection<Capability> CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
            {
                    Capability.CREATE,
                    Capability.DELETE,
                    Capability.RENAME,
                    Capability.GET_TYPE,
                    Capability.LIST_CHILDREN,
                    Capability.READ_CONTENT,
                    Capability.GET_LAST_MODIFIED,
                    Capability.URI,
                    Capability.WRITE_CONTENT,
                    Capability.APPEND_CONTENT,
                    Capability.RANDOM_ACCESS_READ,
            }));

    @Override
    protected FileSystem doCreateFileSystem(FileName name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        final GenericFileName rootName = (GenericFileName) name;
        return new GitFileSystem(rootName,fileSystemOptions);

    }

    @Override
    public Collection<Capability> getCapabilities() {
        return CAPABILITIES;
    }
}
