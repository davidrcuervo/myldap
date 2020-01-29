package com.laetienda.myldap;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test {
	private static final Logger log = LogManager.getLogger(Test.class);
	
	private Ldap ldap;
	
	public Test() {
		ldap = new Ldap();
	}
	
	public void search(String entry, LdapConnection conn) throws Exception {
		log.info("Searching in ldap. $dn: {}", entry);
		Entry result = ldap.searchDn(entry, conn);
		
		if(result == null) {
			log.warn("It didn't find any entry");
		}else {
			for(Attribute attr : result.getAttributes()) {
				log.debug("{} -> {}", attr.getId(), attr.get().getString());
			}
		}
	}
	
	public void add(LdapConnection conn) throws Exception {
		log.info("Testing adding user...");
		User user = new User();
		try {
			user.setUid("testUser", conn);
			user.setCn("User Test");
			user.setSn("snlesses");
			user.setEmail("user.test@mail.com", conn);
			user.setPassword("Welcome1", "Welcome1");
			ldap.insertUser(user, conn);
			log.info("...User added succesfully");
		} catch (Exception e) {
			log.error("Failed to add user. $error: ", e.getMessage());
			throw e;
		}
	}
	
	public void modify(LdapConnection conn) throws Exception {
		log.info("Testing user modification...");
		try {
			User user = ldap.findUser("uid=testUser, ou=people, dc=example, dc=com", conn);
//			user.setEmail("user.test2@mail.com", conn);
			user.setCn("Third Test");
			user.setSn("Third Test");
			user.setPassword("Welcome3", "Welcome3");
			ldap.modify(user, conn);
			log.info("...User test modification has finished succesfully.");
		} catch (Exception e) {
			log.error("Failed to modify user. $error: {}", e.getMessage());
			throw e;
		}
	}
	
	public void remove(LdapConnection conn) throws Exception {
		log.info("Testing remove user from LDAP...");
		
		try {
			User user = ldap.findUser("uid=testUser, ou=people, dc=example, dc=com", conn);
			ldap.removeUser(user, conn);
			log.info("...User removed succesfully.");
		} catch (Exception e) {
			log.warn("Failed to remove user.");
			throw e;
		}
	}
	
	public static void main(String args[]) {
		log.info("Testing LDAP module...");
		
		Test test = new Test();
		LdapManager manager = new LdapManager();
		
		LdapConnection conn = null;
		
		try {
			conn = manager.getLdapConnection("homeServer3.la-etienda.com", 636, "dc=example, dc=com", "cn=admin, dc=example, dc=com", "Welcome1");
			//test.add(conn);
			test.search("uid=testUser, ou=people, dc=example, dc=com", conn);
			test.modify(conn);
			test.search("uid=testUser, ou=people, dc=example, dc=com", conn);
			test.remove(conn);
			test.search("uid=testUser, ou=people, dc=example, dc=com", conn);
			log.info("... LDAP Module test has finished succesfully");
		} catch (Exception e) {
			log.error("Test failed", e);
		} finally {
			manager.closeLdapConnection(conn);
		}
		
		log.info("Java has closed;");
	}
}
