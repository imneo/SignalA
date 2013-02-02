package com.zsoft.SignalA.Transport;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.zsoft.SignalA.Connection;

public class TransportHelper {
    private static final String TAG = "TransportHelper";

    public static ProcessResult ProcessResponse(Connection connection, JSONObject response)
    {
    	ProcessResult result = new ProcessResult();
    	
        if (connection == null)
        {
            //throw new Exception("connection");
        }

        if (response == null)
        {
            return result;
        }

    	String newMessageId = null;
    	JSONArray messagesArray = null;
    	JSONArray resetGroups = null;
    	JSONArray addedGroups = null;
    	JSONArray removedGroups = null;
    	JSONObject transportData = null;
    	JSONObject info = null;

		result.timedOut = response.optInt("T") == 1;	
		result.disconnected = response.optInt("D") == 1;
		newMessageId = response.optString("C");
		messagesArray = response.optJSONArray("M");
		resetGroups = response.optJSONArray("R");
		addedGroups = response.optJSONArray("G");
		removedGroups = response.optJSONArray("g");
		info = response.optJSONObject("I");

		if(info != null)
		{
			
			// ToDo
			//connection.OnReceive(response);
			return result;
		}
		
		if(result.disconnected)
		{
			return result;
		}			
		
		if(resetGroups!=null)
		{
			connection.ResetGroups(TransportHelper.ToArrayList(resetGroups));
		}
		else
		{
			connection.ModifyGroups(TransportHelper.ToArrayList(addedGroups), TransportHelper.ToArrayList(removedGroups));
		}

        if (messagesArray != null)
        {
			for (int i = 0; i < messagesArray.length(); i++) {
				//JSONObject m = null;
				try {
					String m = messagesArray.getString(i); //.getJSONObject(i);
					connection.OnMessage(m.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

            connection.setMessageId(newMessageId);
        }
        
        return result;
    }

    
    
	public static String GetReceiveQueryString(Connection connection, String data, String transport)
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("connection");
        }
        if (transport == null)
        {
            throw new IllegalArgumentException("transport");
        }

    	
        // ?transport={0}&connectionId={1}&messageId={2}&groups={3}&connectionData={4}{5}
		String qs = "?transport=";
		qs += transport;
		qs += "&connectionId=" + connection.getConnectionId();
		if(connection.getMessageId()!=null)
		{
			try {
				qs += "&messageId=" + URLEncoder.encode(connection.getMessageId(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported message encoding error, when encoding messageid.");
			}
		}

		List<String> groups = connection.getGroups();
        if (groups != null && !groups.isEmpty())
        {
        	JSONArray jsArray = new JSONArray(groups);
            try {
				qs += "&messageId=" + URLEncoder.encode(jsArray.toString(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported message encoding error, when encoding groups.");
			}
        }

        if (data != null)
        {
            qs += "&connectionData=" + data;
        }

        return qs;
    }

	
	public static ArrayList<String> ToArrayList(JSONArray jsonArray)
	{
		ArrayList<String> list = null;
		if (jsonArray != null) { 
			int len = jsonArray.length();
			list = new ArrayList<String>(len);     
			for (int i=0;i<len;i++){ 
				Object o = jsonArray.opt(i);
				if(o!=null)
					list.add(o.toString());
			} 
		}
		
		return list;
	}

}
