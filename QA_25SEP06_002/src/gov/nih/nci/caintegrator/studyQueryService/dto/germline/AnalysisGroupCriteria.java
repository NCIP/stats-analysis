package gov.nih.nci.caintegrator.studyQueryService.dto.germline;

/**
 /**
  * Author: Ram Bhattaru
  * Date:   Jul 21, 2006
  * Time:   5:08:50 PM
  */
public class AnalysisGroupCriteria {

    private String[] names;
	public AnalysisGroupCriteria(){}

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

	@Override
	public String toString()
	{
		String str = "AnalysisGroup\n";
		
		if ((names != null) && (names.length > 0))
		{
			for (String name : names)
			{
				str = str + name + "\n";
			}
		}
			
		return str;
	}
}