package com.laetienda.myldap;

import java.util.Iterator;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ldap {
	private final static Logger log = LogManager.getLogger(Ldap.class);
	
	public Ldap() {

	}
	
	public Dn buildDn(String dn) throws LdapInvalidDnException {
		log.info("Building Dn object. $dn: {}", dn);
		Dn result = null;
		
		try {
			result = new Dn(dn);
		} catch (LdapInvalidDnException e) {
			log.warn("Failed to create Dn.");
			throw e;
		}
		
		return result;
	}
	
	/**
	 * @param ldapEntity
	 * @param conn
	 * @throws LdapException
	 */
	public void insertLdapEntity(LdapEntity ldapEntity, LdapConnection conn) throws LdapException {
		log.info("Inserting LdapEntity into ldap...");
		
		try {
			log.debug("LdapEntity Dn. $dn: {}", ldapEntity.getLdapEntry().getDn().getName());
			
			if(log.isDebugEnabled()) {
				Iterator<Attribute> iterator = ldapEntity.getLdapEntry().getAttributes().iterator();
				while(iterator.hasNext()) {
					Attribute atr = iterator.next();
					log.debug("$Attribute: {} -> $Value: {}", atr.getId(), atr.get());
				}
			}
			
			if(ldapEntity.getErrors().size() > 0 ) {
				log.warn("LdapEntity was not added. User input not valid");
			}else {
				conn.add(ldapEntity.getLdapEntry());
				log.info("... LdapEntity has been inserted into ldap succesfully");
			}
		} catch (LdapException e) {
			log.error("Failed to insert LdapEntity into ldap");
			throw e;
		}
	}
	
	/**
	 * 
	 * @param dnstr
	 * @param cn
	 * @return User
	 * @throws Exception 
	 */
	public User findUser(String dnstr, LdapConnection conn) throws Exception{
		Dn dn = buildDn(dnstr);
		return findUser(dn, conn);
	}
	
	/**
	 * 
	 * @param dn
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public User findUser(Dn dn, LdapConnection conn) throws Exception{
		User result = null;
		try {
			Entry entry = conn.lookup(dn);
			result = new User(entry);
		} catch (Exception e) {
			log.warn("Failed to find user");
			throw e;
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param ldapEnity
	 * @param conn
	 * @throws LdapException
	 */
	public void modify(LdapEntity ldapEnity, LdapConnection conn) throws LdapException {
		log.info("Modifying LdapEntity in ldap...");
		try {
			
			if(ldapEnity.getErrors().size() > 0) {
				log.warn("Failed to modify LdapEntity due to invalid user input");
			}else {
				
				for(Modification modification : ldapEnity.getModifications()) {
					conn.modify(ldapEnity.getLdapEntry().getDn(), modification);
				}
				
				ldapEnity.clearModifications();
				
				log.info("... LdapEntity has been modifying succesfully");
			}
		} catch (LdapException e) {
			log.error("Failed to modify LdapEntity in ldap");
			throw e;
		}
	}
	
	public void ldapEntity(LdapEntity ldapEntity, LdapConnection conn) throws LdapException {
		log.info("removing LdapEntity from ldap...");
		
		try {
			conn.delete(ldapEntity.getLdapEntry().getDn());
			log.info("...LdapEntity removed form LDAP succesfully");
		} catch (LdapException e) {
			log.info("failed to remove user from LDAP");
			throw e;
		}
	}
	
	public Entry getPeopleLdapEntry(LdapConnection conn) throws Exception{
		log.info("Getting people ldap entry...");
		Entry result = null;
		
		try {
			Dn peopleDn = new Dn("ou=People", LdapManager.getDomainDn().getName());
			result=conn.lookup(peopleDn);
			log.info("... people Dn has been found. $peopleDn: {}", result.getDn().getName());
		} catch (Exception e) {
			log.warn("failed to get people ldap entry");
			throw e;
		}
		
		return result;
	}
	
//	private void closeCursor(EntryCursor cursor) {
//		log.info("Closing ldap search cursor");
//		try {
//			if(cursor != null && !cursor.isClosed()) {
//				cursor.close();
//			}
//			log.info("... ldap search cursor has closed succesfully");
//		}catch(IOException e) {
//			log.warn("Failed to close ldap search cursor.", e);
//		}
//	}
}
