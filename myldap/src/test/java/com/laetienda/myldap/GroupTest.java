package com.laetienda.myldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GroupTest {
	private static final Logger log = LogManager.getLogger();
	
	private Ldap ldap;
	
	public GroupTest() {
		ldap = new Ldap();
	}
	
	public Group findGroup(String group, LdapConnection conn) {
		
		Dn dn;
		Group result = null;
		try {
			dn = ldap.buildDn("cn=" + group + ", ou=groups, " + LdapManager.getDomainDn().getName());
			result = new Group(dn, conn);
			
			for(Attribute attr : result.getLdapEntry().getAttributes()) {
				log.debug("{} -> {}", attr.getId(), attr.get().getString());
			}
		} catch (LdapException | IOException e) {
			log.warn("Failed to find group. $groupName: {}", group);
			log.debug("Failed to find group. $groupName: {}", e);
		}
		
		return result;
	}
	
	public List<User> findUsers(String groupName, LdapConnection conn){
		
		List<User> result = new ArrayList<User>();
		
		try {
			Group group = new Group(groupName, conn);
			result = group.getMembers(conn);
			for(User user : result) {
				for(Attribute attr : user.getLdapEntry().getAttributes()) {
					log.debug("{} -> {}", attr.getId(), attr.get().getString());
				}
			}
		} catch (Exception e) {
			log.warn("Failed to find group users. $error: {}", e.getMessage());
			log.debug("Failed to find group users.", e);
		}
		
		return result;
	}
	
	public void add(LdapConnection conn) {
		log.info("Testing add group....");
		Group group = new Group();

		try {
			group.setName("Test Group", conn);
			group.setDescription("Testing group");
			group.setOwner("tomcat", conn);
			group.addMember("tomcat", conn);
			ldap.insertLdapEntity(group, conn);
			log.info("Testing adding group has finishes succesfully");
		} catch (Exception e) {
			log.warn("Failed to test add group");
			log.debug("Failed to test add group", e);
		}
	}
	
	public void addUser(String groupName, String user, LdapConnection conn) {
		
		Dn groupDn;
		Dn userDn;
		try {
			groupDn = ldap.buildDn("cn=" + groupName + ",ou=groups," + LdapManager.getDomainDn().getName());
			userDn = new Dn("uid=" + user + ",ou=people," + LdapManager.getDomainDn().getName());
			Group group = new Group(groupDn, conn);
			
			Entry entry = group.getLdapEntry();
			log.debug("$userDn: {}", userDn.getName());
			log.debug("$isUserPartOfGroup: {}", entry.contains("uniqueMember", userDn.getName()));
			log.debug("$groupExits: {}", conn.exists(groupDn));
			
			group.addMember(user, conn);
			ldap.modify(group, conn);
		} catch (LdapException | IOException e) {
			log.warn("Failed to add user to group. $group: {} - $user: {}", groupName, user);
			log.debug("Failed to add user to group", e);
		}
	}
	
	public void removeUser(String groupName, String username, LdapConnection conn) throws Exception {
		Group group = new Group(groupName, conn);
		group.removeMember(username, conn);
		ldap.modify(group, conn);
		this.findUsers(groupName, conn);
	}
	
	public static void main(String[] args) {
		log.info("Testing GROUP LDAP Module...");
		
		GroupTest test = new GroupTest();
		LdapManager manager = new LdapManager();
		LdapConnection conn = null;

		try {
			conn=manager.getLdapConnection("homeServer3.la-etienda.com", 636, "dc=example,dc=com", "cn=admin,dc=example,dc=com", "Welcome1");
//			test.add(conn);
//			test.addUser("Test Group", "manager", conn);
//			test.findGroup("Test Group", conn);
			test.findUsers("Test Group", conn);
//			test.removeUser("Test Group", "manager", conn);
			test.removeUser("Test Group", "tomcat", conn);
		} catch (Exception e) {
			log.error("Test failed.", e);
		} finally {
			manager.closeLdapConnection(conn);
		}
		
		log.info("Java has closed");
	}

}
