package org.notima.camel.fortnox.junit;

import org.notima.api.fortnox.FortnoxKeyProvider;
import org.notima.api.fortnox.clients.FortnoxApiKey;

public class MockKeyProvider extends FortnoxKeyProvider {

    public MockKeyProvider(String orgNo) {
        super(orgNo);
        //TODO Auto-generated constructor stub
    }

    @Override
    public FortnoxApiKey getKey() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setKey(FortnoxApiKey key) throws Exception {
        // TODO Auto-generated method stub
        
    }
    
}
