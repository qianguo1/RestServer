package com.asiainfo.ocmanager.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;

/**
 * LDAP context
 * 
 * @author EthanWang
 *
 */
public class LdapWrapper {
	private static final Logger LOG = Logger.getLogger(LdapWrapper.class);
	private static Properties props;
	private static SearchControls cons;
	private static JndiLdapContextFactory factory;

	/**
	 * Get available users in Ldap server.
	 * 
	 * @return
	 */
	public static List<String> allUsers() {
		try {
			List<String> users = new ArrayList<>();
			NamingEnumeration<SearchResult> results = searchUsers();
			while (results.hasMore()) {
				SearchResult ele = results.next();
				users.add((String) ele.getAttributes().get("uid").get());
			}
			return users;
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	private static void initClz() {
		InputStream inputStream = null;
		try {
			String base = LdapWrapper.class.getResource("/").getPath() + ".." + File.separator;
			String confpath = base + "conf" + File.separator + "ldap.properties";
			inputStream = new FileInputStream(new File(confpath));
			props = new Properties();
			props.load(inputStream);
		} catch (Exception e) {
			LOG.error("Error while locating file ldap.properties: ", e);
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static {
		try {
			initClz();
			factory = new JndiLdapContextFactory();
			factory.setUrl(props.getProperty("ldap.url").trim());
			cons = new SearchControls();
			cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
			cons.setReturningAttributes(new String[] { "uid" });
		} catch (Throwable e) {
			LOG.error("Error while init class: ", e);
			throw new RuntimeException(e);
		}
	}

	private static NamingEnumeration<SearchResult> searchUsers() {
		LdapContext ctx = null;
		try {
			ctx = factory.getSystemLdapContext();
			return ctx.search(props.getProperty("ldap.base.name").trim(), props.getProperty("ldap.search.filter"),
					cons);
		} catch (Exception e) {
			LOG.error("Failed to get Ldap users: ", e);
			throw new RuntimeException(e);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
