package com.github.mostroverkhov.firebase_rsocket.internal.auth.sources;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsSource;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.cred_suppliers.FsPathCredentialsSupplier;
import com.github.mostroverkhov.firebase_rsocket.internal.auth.cred_factories.PropCredentialsFactory;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class FsPathPropsCredentialsSource extends CredentialsSource {
    public FsPathPropsCredentialsSource(String fsFileName) {
        super(new FsPathCredentialsSupplier(fsFileName), new PropCredentialsFactory());
    }
}
