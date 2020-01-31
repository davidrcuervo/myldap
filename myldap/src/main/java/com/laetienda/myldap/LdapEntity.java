package com.laetienda.myldap;

import java.util.HashMap;
import java.util.List;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;

public interface LdapEntity {
	
	public Entry getLdapEntry();
	public HashMap<String, List<String>> getErrors();
	public List<Modification> getModifications();
	public void clearModifications();

}
