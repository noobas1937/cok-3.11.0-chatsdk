package com.elex.chatservice.view.allianceshare.model;

import java.io.IOException;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseException;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.utilities.encoding.JsonHelpers;

public class AllianceShareDataSnapshot extends DataSnapshot
{

	private final IndexedNode	node;
	public AllianceShareDataSnapshot(Firebase ref, IndexedNode node)
	{
		super(ref, node);
		this.node = node;
	}

	public Object getValue(Class valueType)
	{
		if(node== null || node.getNode() == null || node.getNode().getValue()==null)
			return null;
		Object value = node.getNode().getValue();
		try
		{
			String json = JsonHelpers.getMapper().writeValueAsString(value);
			return JsonHelpers.getMapper().readValue(json, valueType);
		}
		catch (IOException e)
		{
			throw new FirebaseException("Failed to bounce to type", e);
		}
	}
}
