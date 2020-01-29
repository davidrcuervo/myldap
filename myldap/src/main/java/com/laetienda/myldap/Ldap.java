package com.laetienda.myldap;

import java.io.IOException;
import java.util.Iterator;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.SearchScope;
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
	
	public Entry searchDn(String dnstr, LdapConnection conn) throws Exception{
		
		Entry result = null;
		
		try {
			Dn dn = new Dn(dnstr);
			result = searchDn(dn,conn);
			
		} catch (Exception e) {
			throw e;
		}
		
		return result;
	}
	
	public Entry searchDn(Dn dn, LdapConnection conn) throws Exception {
		log.info("Searching for dn entry in LDAP. $dn: {} ...", dn.getName());
		Entry result = null;
		EntryCursor cursor = null;;
		try {
			cursor = conn.search(dn, "(objectclass=*)",  SearchScope.OBJECT);
			
			if(cursor.next()) {
				result = cursor.get();
			}
			
			if(cursor.iterator().hasNext()) {
				result = null;
				log.warn("dn: %s exists more than once", dn);
			}
			
			log.info("... sarch for dn has finished");
		} catch (LdapException  | CursorException e) {
			log.warn("Failed to search dn entry.");
			throw e;
		} finally {
			closeCursor(cursor);
		}
		
		return result;
	}
	
	/**
	 * @param user
	 * @param conn
	 * @throws LdapException
	 */
	public void insertUser(User user, LdapConnection conn) throws LdapException {
		log.info("Inserting user into ldap...");
		
		try {
			log.debug("User Dn. $dn: {}", user.getLdapEntry().getDn().getName());
			
			if(log.isDebugEnabled()) {
				Iterator<Attribute> iterator = user.getLdapEntry().getAttributes().iterator();
				while(iterator.hasNext()) {
					Attribute atr = iterator.next();
					log.debug("$Attribute: {} -> $Value: {}", atr.getId(), atr.get());
				}
			}
			
			if(user.getErrors().size() > 0 ) {
				log.warn("User was not added. User input not valid");
			}else {
				conn.add(user.getLdapEntry());
				log.info("... user has been inserted into ldap succesfully");
			}
		} catch (LdapException e) {
			log.error("Failed to insert user into ldap");
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
			Entry entry = searchDn(dn, conn);
			result = new User(entry);
		} catch (Exception e) {
			log.warn("Failed to find user");
			throw e;
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param user
	 * @param conn
	 * @throws LdapException
	 */
	public void modify(User user, LdapConnection conn) throws LdapException {
		log.info("Modifying user in ldap...");
		try {
			
			if(user.getErrors().size() > 0) {
				log.warn("Failed to modify user due to invalid user input");
			}else {			
				for(Modification modification : user.getModifications()) {
					conn.modify(user.getLdapEntry().getDn(), modification);
				}
				log.info("... user has been modifying succesfully");
			}
		} catch (LdapException e) {
			log.error("Failed to modify user in ldap");
			throw e;
		}
	}
	
	public void removeUser(User user, LdapConnection conn) throws LdapException {
		log.info("removing user from ldap...");
		
		try {
			conn.delete(user.getLdapEntry().getDn());
			log.info("...User removed form LDAP succesfully");
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
			result=searchDn(peopleDn, conn);
			log.info("... people Dn has been found. $peopleDn: {}", result.getDn().getName());
		} catch (Exception e) {
			log.warn("failed to get people ldap entry");
			throw e;
		}
		
		return result;
	}
	
	private void closeCursor(EntryCursor cursor) {
		log.info("Closing ldap search cursor");
		try {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			log.info("... ldap search cursor has closed succesfully");
		}catch(IOException e) {
			log.warn("Failed to close ldap search cursor.", e);
		}
	}
}
