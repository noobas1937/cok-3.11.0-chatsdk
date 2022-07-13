package com.elex.chatservice.view.emoj;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;

public class DefaultEmojDatas {
	
	public static final String 		DEFAULT_EMOJ_GROUP_ID	 = "100";
    
	private static int[] icons = new int[]{
        R.drawable.emoj15,  
        R.drawable.emoj16,  
        R.drawable.emoj17,  
        R.drawable.emoj18,  
        R.drawable.emoj19,  
    };
    
    private static int[] bigIcons = new int[]{
        R.drawable.emoj15,  
        R.drawable.emoj16,  
        R.drawable.emoj17,  
        R.drawable.emoj18,  
        R.drawable.emoj19,  
    };
    
    
    private static final EmojGroupEntity DATA = createData();
    
    private static EmojGroupEntity createData(){
        EmojGroupEntity emojGroupEntity = new EmojGroupEntity();
        EmojIcon[] datas = new EmojIcon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new EmojIcon(icons[i], "", EmojIcon.Type.BIG_EMOJ);
            datas[i].setBigIcon(bigIcons[i]);
            datas[i].setName("");
            datas[i].setGroupId(DEFAULT_EMOJ_GROUP_ID);
            datas[i].setId(""+(i+1));
        }
        emojGroupEntity.setGroupId(DEFAULT_EMOJ_GROUP_ID);
        emojGroupEntity.setDetails(Arrays.asList(datas));
        emojGroupEntity.setIcon(R.drawable.emoj15);
        emojGroupEntity.setType(EmojIcon.Type.BIG_EMOJ);
        return emojGroupEntity;
    }
    
    public static EmojIcon getEmoj(String groupId,String id)
    {
    	if(StringUtils.isEmpty(groupId) || DATA == null || StringUtils.isEmpty(DATA.getGroupId()) || !DATA.getGroupId().equals(groupId) || DATA.getDetails() == null || DATA.getDetails().size() == 0)
    		return null;
    	if(StringUtils.isNumeric(id))
    	{
    		int index = Integer.parseInt(id);
    		if(index>0 && index<=DATA.getDetails().size())
    			return DATA.getDetails().get(index - 1);
    	}
    	return null;
    }
    
    public static EmojGroupEntity getData(){
        return DATA;
    }
}
