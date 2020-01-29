package com.laetienda.myldap;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Installer {
	final static Logger log = LogManager.getLogger(Installer.class);
	
	public void install(String address, int port, String domain, String user, String password) throws LdapException {
		log.info("Installing LDAP directory...");
		
		LdapManager ldapManager = new LdapManager();
		LdapConnection conn = null;
		try {
			conn = ldapManager.getLdapConnection(address, port, domain, user, password);
			
			log.info("... LDAP directory installed succesfully");
		} catch (LdapException e) {
			log.error("Failed to install LDAP directory");
			throw e;
		}finally {
			ldapManager.closeLdapConnection(conn);
		}
				
		log.info("... LDAP Directory has been installed succesfully");
	}

	public static void main(String[] args) {
		log.info("Running LDAP installer is starting....");
		
		Installer installer = new Installer();
		
		try {
			installer.install("homeServer3.la-etienda.com", 636, "dc=example, dc=com", "cn=admin,dc=example,dc=com", "Welcome1");
			log.info("...LDAP installer finish succesfully");
		} catch (LdapException e) {
			log.error("Failed to install LDAP", e);
		}
	}
}
