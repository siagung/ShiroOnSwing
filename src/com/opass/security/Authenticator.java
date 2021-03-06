package com.opass.security;

import javax.swing.JOptionPane;
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory; 
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.authc.credential.Md5CredentialsMatcher;
import org.apache.shiro.session.Session;

/**
 *
 * @author zakaria
 */
public class Authenticator {
    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);  
    

    private static final String AUTHENTICATION_QUERY = "SELECT password FROM users WHERE username=?";

    private static final String SALTED_AUTHENTICATION_QUERY = "SELECT password, salt FROM users WHERE username = ?";
    private static final String ROLES_QUERY = "SELECT g.name FROM groups g,users u WHERE g.id = u.group_id AND u.username=?";
    private static final String PERMISIONS_QUERY = "SELECT p.permission FROM perms p,groups g WHERE p.group_id=g.id AND g.name=?";


    /**
     * * Autenticates a user *
     * @param username
     * @param pass
     * @return 
     */
    public Subject authenticate(String username, String pass) {
        
        //Md5Hash md5u = new Md5Hash(username);
        //String passw= pass;
        Subject currentUser = null;
        try {

            Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
            org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();

            // init shiro - place this e.g. in the constructor
            // SecurityUtils.setSecurityManager(securityManager);
            // the key "jdbcRealm" must be the same in the shiro.ini file.

            JdbcRealm realm = (JdbcRealm)((IniSecurityManagerFactory) factory)
                    .getBeans().get("jdbcRealm");
   

            Md5CredentialsMatcher md5 = new Md5CredentialsMatcher();
            md5.setStoredCredentialsHexEncoded(true);
            realm.setCredentialsMatcher(md5);
            realm.setAuthenticationQuery(AUTHENTICATION_QUERY);
            realm.setUserRolesQuery(ROLES_QUERY);
            realm.setPermissionsQuery(PERMISIONS_QUERY);
            realm.setSaltStyle(JdbcRealm.SaltStyle.NO_SALT);
            realm.setPermissionsLookupEnabled(true);
            SecurityUtils.setSecurityManager(securityManager);
            currentUser = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(username, pass);
 
            // return info;
            try {
                currentUser.login(token);
                System.out.println("User [" + currentUser.getPrincipal().toString() + "] logged in successfully.");
         
            // save current username in the session, so we have access to our User model
                Session session = currentUser.getSession();  
                session.setAttribute("username", username);
              
            } catch (UnknownAccountException uae) {
                log.info("There is no user with username of " + token.getPrincipal());  
                JOptionPane.showMessageDialog(null,"There is no user with username of \" "
                        + token.getPrincipal() + "\"");
                //System.out.println("There is no user with username of "
                    //    + token.getPrincipal());
            } catch (IncorrectCredentialsException ice) {
                  JOptionPane.showMessageDialog(null,"Password for account " + token.getPrincipal()
                 + " was incorrect!");
//                System.out.println("Password for account " + token.getPrincipal()
//                        + " was incorrect!");
            } catch (LockedAccountException lae) {
                JOptionPane.showMessageDialog(null,"The account for username " + token.getPrincipal()
                        + " is locked.  "
                        + "Please contact your administrator to unlock it.");
//                System.out.println("The account for username " + token.getPrincipal()
//                        + " is locked.  "
//                        + "Please contact your administrator to unlock it.");
            } catch (ExcessiveAttemptsException aee) {
                JOptionPane.showMessageDialog(null,aee);                
//System.out.println("eae : " + eae);
            } catch (AuthenticationException ae) {
                JOptionPane.showMessageDialog(null,ae);  
                //System.out.println("ae : " + ae);
            }
        } catch (Exception e) {
        }
        return currentUser;

    }

}
