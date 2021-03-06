package vjo.security;

/*
 * @(#)src/classes/sov/java/security/UnresolvedPermissionCollection.java, security, asdev, 20070119 1.14
 * ===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) 2 Technology Edition, v5.0
 * (C) Copyright IBM Corp. 1998, 2005. All Rights Reserved
 * ===========================================================================
 */

/*
 * ===========================================================================
 (C) Copyright Sun Microsystems Inc, 1992, 2004. All rights reserved.
 * ===========================================================================
 */
import java.lang.ClassNotFoundException;
import java.lang.IllegalArgumentException;

import java.util.Enumeration;
import java.util.Iterator; 
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import java.io.IOException;
import java.io.Serializable;

import vjo.lang.* ;

import vjo.util.ArrayList ;
import vjo.util.Collections ;
import vjo.util.HashMap ;
import vjo.util.Hashtable ;
import vjo.util.Vector ;

import vjo.io.ObjectStreamField;
import vjo.io.ObjectOutputStream;
import vjo.io.ObjectInputStream;


/**
 * A UnresolvedPermissionCollection stores a collection
 * of UnresolvedPermission permissions.
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.UnresolvedPermission
 *
 * @version 1.14 03/12/19
 *
 * @author Roland Schemers
 *
 * @serial include
 */

final class UnresolvedPermissionCollection
extends PermissionCollection
implements Serializable
{
    /**
     * Key is permission type, value is a list of the UnresolvedPermissions
     * of the same type.
     * Not serialized; see serialization section at end of class.
     */
    private transient Map perms;

    /**
     * Create an empty UnresolvedPermissionCollection object.
     *
     */
    public UnresolvedPermissionCollection() {
        perms = new HashMap(11);
    }

    /**
     * Adds a permission to this UnresolvedPermissionCollection. 
     * The key for the hash is the unresolved permission's type (class) name.
     *
     * @param permission the Permission object to add.
     */

    public void add(Permission permission)
    {
        if (! (permission instanceof UnresolvedPermission))
            throw new IllegalArgumentException("invalid permission: "+
                                               permission);
        UnresolvedPermission up = (UnresolvedPermission) permission;

        List v;
        synchronized (this) {
            v = (List) perms.get(up.getName());
            if (v == null) {
                v = new ArrayList();
                perms.put(up.getName(), v);
            }
        }
        synchronized (v) {
            v.add(up);
        }
    }

    /**
     * get any unresolved permissions of the same type as p,
     * and return the List containing them.
     */
    List getUnresolvedPermissions(Permission p) {
        synchronized (this) {
            return (List) perms.get(p.getClass().getName());
        }
    }

    /**
     * always returns false for unresolved permissions
     *
     */
    public boolean implies(Permission permission)
    {
        return false;
    }

    /**
     * Returns an enumeration of all the UnresolvedPermission lists in the
     * container.
     *
     * @return an enumeration of all the UnresolvedPermission objects.
     */

    public Enumeration elements() {
        List results = new ArrayList(); // where results are stored

        // Get iterator of Map values (which are lists of permissions)
        synchronized (this) {
            for (Iterator iter = perms.values().iterator(); iter.hasNext();) {
                List l = (List) iter.next();
                synchronized (l) {
                    results.addAll(l);
                }
            }
        }

        return Collections.enumeration(results);
    }

    private static final long serialVersionUID = -7176153071733132400L;

    // Need to maintain serialization interoperability with earlier releases,
    // which had the serializable field:
    // private Hashtable permissions; // keyed on type

    /**
     * @serialField permissions java.util.Hashtable
     *     A table of the UnresolvedPermissions keyed on type, value is Vector
     *     of permissions
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Hashtable.class),
    };

    /**
     * @serialData Default field.
     */
    /*
     * Writes the contents of the perms field out as a Hashtable 
     * in which the values are Vectors for
     * serialization compatibility with earlier releases.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // Don't call out.defaultWriteObject()

        // Copy perms into a Hashtable
        Hashtable permissions = new Hashtable(perms.size()*2);

        // Convert each entry (List) into a Vector
        synchronized (this) {
            Set set = perms.entrySet();
            for (Iterator iter = set.iterator(); iter.hasNext(); ) {
                Map.Entry e = (Map.Entry)iter.next();

                // Convert list into Vector
                List list = (List) e.getValue();
                Vector vec = new Vector(list.size());
                synchronized (list) {
                    vec.addAll(list);
                }

                // Add to Hashtable being serialized
                permissions.put(e.getKey(), vec);
            }
        }

        // Write out serializable fields
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("permissions", permissions);
        out.writeFields();
    }

    /*
     * Reads in a Hashtable in which the values are Vectors of
     * UnresolvedPermissions and saves them in the perms field. 
     */
    private void readObject(ObjectInputStream in) throws IOException, 
    ClassNotFoundException {
        // Don't call defaultReadObject()

        // Read in serialized fields
        ObjectInputStream.GetField gfields = in.readFields();

        // Get permissions
        Hashtable permissions = (Hashtable)gfields.get("permissions", null);
        perms = new HashMap(permissions.size()*2);

        // Convert each entry (Vector) into a List
        Set set = permissions.entrySet();
        for (Iterator iter = set.iterator(); iter.hasNext(); ) {
            Map.Entry e = (Map.Entry)iter.next();

            // Convert Vector into ArrayList
            Vector vec = (Vector) e.getValue();
            List list = new ArrayList(vec.size());
            list.addAll(vec);

            // Add to Hashtable being serialized
            perms.put(e.getKey(), list);
        }
    }
}
