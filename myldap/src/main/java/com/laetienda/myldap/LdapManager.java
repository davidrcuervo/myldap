package com.laetienda.myldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LdapManager {
	final static Logger log = LogManager.getLogger(LdapManager.class);
	
	private static Dn domainDn;
	private List<LdapConnection> connections = new ArrayList<LdapConnection>();
	
	private static void setDomainDn(Dn dn) {
		domainDn = dn;
	}
	
	public static Dn getDomainDn() {
		return domainDn;
	}
	
	public synchronized LdapConnection getLdapConnection(String address, int port, Dn domainDn, Dn userdn, String password) throws LdapException {
		log.info("Creating ldap connection...");
		log.debug("Connecting to ldap. $address: {} - $port {} - $userdn: {}", address, Integer.toString(port), userdn.getName());
		
		setDomainDn(domainDn);
		
		LdapConnection result = new LdapNetworkConnection(address, port, true);
		
		try {
			result.bind(userdn, password);
			log.debug("$RootDSE: {}", result.getRootDse().getDn().getName());
			connections.add(result);
			log.info("...ldap connection created succesfully");
		} catch (LdapException e) {
			log.error("Failed to connect to ldap server");
			throw e;
		}
		
		return result;
	}
	
	public LdapConnection getLdapConnection(String address, int port, String domain, String user, String password) throws LdapException {
		
		LdapConnection result = null;
		
		try {
			Dn userdn = new Dn(user);
			Dn domainDn = new Dn(domain);
			result = getLdapConnection(address, port, domainDn, userdn, password);
		} catch (LdapInvalidDnException e) {
			log.error("Invalid user format. $user: " + user);
			throw e;
		}
		
		return result;
	}
	
	public void closeLdapConnection(LdapConnection connection){
		log.info("Closing Ldap Connection...");
		
		try {
			if(connection != null) {
				if(connection.isConnected() || connection.isAuthenticated()) {
						connection.unBind();
				}
				
				if(connection.isConnected()){
					connection.close();
				}
			}
		
			connections.remove(connection);
		} catch (LdapException | IOException e) {
			log.error("Failed to close connection", e);
		}
		
		log.info("...Ldap Connection closed succesfully");
	}
	
	public static void main(String args[]) {
		log.info("Testing connection to LDAP server...");
		
		LdapManager manager = new LdapManager();
		LdapConnection conn = null;
		
		try {
			conn = manager.getLdapConnection("homeServer3.la-etienda.com", 636, "dc=example, dc=com", "cn=admin,dc=example,dc=com", "Welcome1");
			log.info("... connection to LDAP server tested succesfully");
		}catch (LdapException e) {
			log.error("Failed to test connection to LDAP server", e);
		}finally {
			manager.closeLdapConnection(conn);
		}		
	}
}
