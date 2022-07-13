package com.elex.chatservice.view.emoj;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;

public class GifEmojDatas {
	
	public static final String 		GIF_EMOJ_GROUP_ID	 = "101";
	
	private static int[] icons = new int[]{
//        R.drawable.icon_002_cover,  
//        R.drawable.icon_007_cover,  
//        R.drawable.icon_010_cover,  
//        R.drawable.icon_012_cover,  
//        R.drawable.icon_013_cover,  
//        R.drawable.icon_018_cover,  
//        R.drawable.icon_019_cover,  
//        R.drawable.icon_020_cover,  
//        R.drawable.icon_021_cover,  
//        R.drawable.icon_022_cover,  
//        R.drawable.icon_024_cover,  
//        R.drawable.icon_027_cover,  
//        R.drawable.icon_029_cover,  
//        R.drawable.icon_030_cover,  
//        R.drawable.icon_035_cover,  
//        R.drawable.icon_040_cover,  
    };
    
    private static int[] bigIcons = new int[]{
//        R.drawable.icon_002,  
//        R.drawable.icon_007,  
//        R.drawable.icon_010,  
//        R.drawable.icon_012,  
//        R.drawable.icon_013,  
//        R.drawable.icon_018,  
//        R.drawable.icon_019,  
//        R.drawable.icon_020,  
//        R.drawable.icon_021,  
//        R.drawable.icon_022,  
//        R.drawable.icon_024,  
//        R.drawable.icon_027,  
//        R.drawable.icon_029,  
//        R.drawable.icon_030,  
//        R.drawable.icon_035,  
//        R.drawable.icon_040,  
    };
    
    
    private static final EmojGroupEntity DATA = createData();
    
    private static EmojGroupEntity createData(){
        EmojGroupEntity emojGroupEntity = new EmojGroupEntity();
        EmojIcon[] datas = new EmojIcon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new EmojIcon(icons[i], null, EmojIcon.Type.BIG_EMOJ);
            datas[i].setBigIcon(bigIcons[i]);
//            datas[i].setName("Icon"+ (i+1));
            datas[i].setGroupId(GIF_EMOJ_GROUP_ID);
            datas[i].setId(""+(i+1));
        }
        emojGroupEntity.setGroupId(GIF_EMOJ_GROUP_ID);
        emojGroupEntity.setDetails(Arrays.asList(datas));
//        emojGroupEntity.setIcon(R.drawable.icon_002_cover);
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
