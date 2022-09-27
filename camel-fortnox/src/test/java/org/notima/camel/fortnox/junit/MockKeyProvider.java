package org.notima.camel.fortnox.junit;

import java.util.List;

import org.notima.api.fortnox.FortnoxCredentialsProvider;
import org.notima.api.fortnox.clients.FortnoxCredentials;

public class MockKeyProvider extends FortnoxCredentialsProvider {

    public MockKeyProvider(String orgNo) {
        super(orgNo);
        //TODO Auto-generated constructor stub
    }

    @Override
    public FortnoxCredentials getCredentials() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCredentials(FortnoxCredentials key) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAllCredentials() throws Exception {
        // TODO Auto-generated method stub
        
    }

	@Override
	public List<FortnoxCredentials> getAllCredentials() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeCredential(FortnoxCredentials removeThis) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int removeCredentials(List<FortnoxCredentials> removeThese) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
    
}
