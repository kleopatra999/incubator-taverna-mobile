package org.apache.taverna.mobile.utils;
/**
 * Apache Taverna Mobile
 * Copyright 2015 The Apache Software Foundation
 *
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Larry Akah
 * @version 1.0.0
 *          This class is developed as a means to simplify the code base of the initial json_db
 *          implementation.
 *          It aims at using fully the java JSON API available to implement the CRUD functionality
 *          of the json_db library.
 *          This class is much more scalable and light-weight than the first implementation
 */
public class WorkflowDB {

    private static final String TAG = "WorkflowDB";
    private final String ENTITY_KEY;
    private final ArrayList<String> ITEM_IDS;
    private Context context;
    private SharedPreferences msharedpreference;
    private JSONObject dataobj; //hold all entries for a given ENTITY_KEY

    /**
     * Constructor initializes a basic data store environment
     *
     * @param ctx       a reference to the application's context or sand box
     * @param entityKey The main data store key for each entity space/schema
     */
    public WorkflowDB(Context ctx, String entityKey) {
        context = ctx;
        ENTITY_KEY = entityKey;
        ITEM_IDS = new ArrayList<String>();
        dataobj = new JSONObject();
    }

    /**
     * Inserts an entity set('row') into the data store
     *
     * @param items values for each 'column' in the entity
     * @return the same instance for chaining multiple calls to this method.
     */
    public WorkflowDB put(ArrayList<Object> items) throws JSONException {

        String item_id = this.generateRandomId();
        ITEM_IDS.add(item_id);
        JSONArray jarray = new JSONArray();
        for (Object item : items) {
            jarray.put(item);
        }
        dataobj.put(item_id, jarray);
        return this;
    }

    /**
     * Returns all entity entries from the data store. Each item of an entity is accompanied with
     * the key or unique id of the items.
     *
     * @throws org.json.JSONException for errors during construction of a JSON data string.
     * @throws NullPointerException   for any null accessed variable
     * @author Larry Akah
     */
    public List<ArrayList<Object>> get() throws JSONException, NullPointerException {
        msharedpreference = PreferenceManager.getDefaultSharedPreferences(context);
        //read key and get existing data
        List<ArrayList<Object>> results = new ArrayList<ArrayList<Object>>();
        JSONObject mainJson = new JSONObject(msharedpreference.getString(ENTITY_KEY, "{" +
                ENTITY_KEY + ":{}}"));

        Log.i(ENTITY_KEY, mainJson.toString(2));

        JSONArray keysJson = mainJson.getJSONArray("ids"); //retrieve a json array of ids for
        // every entity entry
        Log.i(ENTITY_KEY, keysJson.toString(2));

        if (null != keysJson)
            for (int i = 0; i < keysJson.length(); i++) {
                //for each key, get the associated data
                try {
                    JSONArray resultArray = mainJson.getJSONArray(keysJson.getString(i));
                    ArrayList<Object> mlist = new ArrayList<Object>();
                    if (null != resultArray)
                        for (int j = 0; j < resultArray.length(); j++) {
                            mlist.add(resultArray.getString(j));

                        }
                    mlist.add(keysJson.getString(i)); // adds the entry key as last value of the
                    // data returned
                    results.add(mlist);
                } catch (Exception e) {
                    Log.e(TAG, "get: ", e);
                    continue;
                }
            }
        return results;
    }

    /**
     * updates a single item entry of an entity
     *
     * @param itemId  provides the id of the item to be updated
     * @param newItem the new set of data to be used to update the entry
     * @return boolean indicating whether the item was successfully updated or not. True means item
     * was updated, false means otherwise.
     */
    public boolean update(String itemId, ArrayList<Object> newItem) {
        boolean operationSucceeded = false;

        JSONArray jarray = new JSONArray();
        for (Object item : newItem) {
            jarray.put(item);
        }
        try {
            dataobj.put(itemId, jarray); //replace the current entry at the given ID.
            Log.d(TAG, "" + dataobj.toString(2));
            operationSucceeded = true;
        } catch (JSONException e) {
            Log.e(TAG, "update: ", e);
            operationSucceeded = false;
        }
        return operationSucceeded;

    }

    /**
     * Updates all the items of a given entity
     *
     * @return the number of items successfully updated.
     */
    public int updateAll(List<ArrayList<String>> items) {
        return 0;
    }

    /**
     * Gets an entry of an entity from the data store
     *
     * @param id The id of the item('row') in question to return
     * @throws org.json.JSONException for errors during construction of a JSON data string.
     * @throws NullPointerException   for any null accessed variable
     * @author Larry Akah
     */
    public ArrayList<Object> get(String id) throws JSONException, NullPointerException {
        msharedpreference = PreferenceManager.getDefaultSharedPreferences(context);
        //read key and get existing data
        ArrayList<Object> results = new ArrayList<Object>();
        JSONObject mainJson = new JSONObject(msharedpreference.getString(ENTITY_KEY, ENTITY_KEY +
                ":{}"));

        Log.i(ENTITY_KEY, mainJson.toString(2));

        JSONArray keysJson = mainJson.getJSONArray("ids"); //retrieve a json array of ids for
        // every entity entry
        Log.i(ENTITY_KEY, keysJson.toString(2));

        if (null != keysJson)
            for (int i = 0; i < keysJson.length(); i++) {
                //for each key, get the associated data
                try {
                    JSONArray resultArray = mainJson.getJSONArray(keysJson.getString(i));
                    if (null != resultArray)
                        for (int j = 0; j < resultArray.length(); j++) {
                            results.add(resultArray.getString(j));
                        }
                } catch (Exception e) {
                    Log.e(TAG, "get: ", e);
                    continue;
                }
            }
        return results;
    }

    /**
     * Persists all data by making the data permanent in the preference file on the fileSystem
     * Save all id's in a different preference
     *
     * @return true or false indicating whether the save was successful or not
     * @author Larry Akah
     */
    public boolean save() {
        msharedpreference = PreferenceManager.getDefaultSharedPreferences(context);
        boolean saved = false;
        if (dataobj != null) {
            try {
                saved = saveid(ITEM_IDS);
                if (saved)
                    msharedpreference.edit().putString(ENTITY_KEY, dataobj.toString()).apply();
                return saved;
            } catch (JSONException e) {
                Log.e(TAG, "save: ", e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @return the number of entities inserted
     */
    public int insert(ArrayList<Object> item) {
        long start = System.currentTimeMillis();
        msharedpreference = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            JSONObject jsonObject = new JSONObject(msharedpreference.getString(ENTITY_KEY, "{" +
                    ENTITY_KEY + ":{}}")); //main json db
            Log.d(TAG, jsonObject.toString(1));

            JSONArray jsonArray;
            if (jsonObject.has("ids")) {
                jsonArray = jsonObject.optJSONArray("ids");
            } else {
                jsonArray = new JSONArray();
            }
            String newItemId = item.get(0).toString(); //use the workflow id as an entity key for
            // the new entity

            //verify if this workflow item has already been marked as favorite
            for (int k = 0; k < jsonArray.length(); k++) {
                if (jsonArray.get(k).toString().equalsIgnoreCase(newItemId))
                    return -1;
            }

            jsonArray.put(jsonArray.length(), newItemId); //add new entity id
            JSONArray newEntity = new JSONArray();
            for (Object entity : item) {
                newEntity.put(entity);
            }
            jsonObject.put("ids", jsonArray);
            jsonObject.put(newItemId, newEntity);
            msharedpreference.edit().putString(ENTITY_KEY, jsonObject.toString()).commit();
            long end = System.currentTimeMillis();
            Log.d(TAG, "Insert benchmark length = " + (end - start));
            return 1;
        } catch (JSONException e) {
            Log.e(TAG, "insert: ", e);
        }
        return 0;
    }

    /**
     * save the ids of all entity entries
     *
     * @param ids A list of auto-generated ids that point to each set of entity data in the data
     *            store
     * @author Larry Akah
     */
    private boolean saveid(ArrayList<String> ids) throws JSONException {
        JSONArray jarray = new JSONArray();
        for (Object item : ids) {
            jarray.put(item);
        }
        dataobj.put("ids", jarray);

        return jarray.length() == ids.size() ? true : false;
    }

    /**
     * Removes an item from an entity entry
     *
     * @author Larry Akah
     */
    public WorkflowDB delete(String itemID) throws JSONException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        JSONObject dbjson = new JSONObject(sp.getString(ENTITY_KEY, ENTITY_KEY + ":{}"));

        JSONArray currentkeys = dbjson.getJSONArray("ids");
        JSONArray newKeys = DBUtility.removeKey(currentkeys, itemID);

        dbjson.put("ids", newKeys);
        sp.edit().putString(ENTITY_KEY, dbjson.toString()).apply();

        return this;
    }

    /**
     * Removes all items from an entity
     *
     * @author Larry Akah
     */
    public int bulkDelete(String entity_key) {

        return 0;
    }

    /**
     * Generates a random hexadecimal string to be used as ids for identifying each entry of an
     * entity item. Proven to be collision resistant enough accross API KEYS
     *
     * @author Larry Akah
     */
    private String generateRandomId() {
        return UUID.randomUUID().toString();
    }
}