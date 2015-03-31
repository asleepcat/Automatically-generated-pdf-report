package bme.lis.common.util.PDFtemplate;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.hibernate.Session;


import bme.lis.basic.service.BaseService;
import bme.lis.common.service.GeneratePDFService;
import bme.lis.common.service.impl.GeneratePDFServiceImpl;
import bme.lis.common.util.PDFMethod;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * 
 * @author 张昊     
 * 日期：2014.07.19
 * 功能：生化模版的处理。对于生化模版的衍生模版采用biochemistry_**的模式命名。
 * 注：biochemistry——生化模版
 * 
 */
public class biochemistry {
	/**
	 * 生化报告单
	 * 
	 * 序号NO，项目FCheckName，结果FResult，
	 * 单位FUnit，参考值FReference，检验方法FMethod
	 * 
	 */
	@SuppressWarnings("rawtypes")
//	private BaseService baseService;
	public int rows;//定义产生的页数
	public static Float FMarginsL;//二维码的X坐标 
	public static Float FMarginsR;//二维码的Y坐标 
	public static Float FMARGINST;//急标识的X坐标 
	public static Float FMARGINSD;//急标识的Y坐标 
	public void setSH(String ID,String inpath,String outpath,GeneratePDFService baseService,Session session){
		   rows=GeneratePDFServiceImpl.rows;
		   FMarginsL=GeneratePDFServiceImpl.FMarginsL;
		   FMarginsR=GeneratePDFServiceImpl.FMarginsR;
		   FMARGINST=GeneratePDFServiceImpl.FMARGINST;
		   FMARGINSD=GeneratePDFServiceImpl.FMARGINSD;
		   
		   /**查询所需信息*/
		   String basesql= PDFMethod.queryBaseValue(ID);
		   List<?> baselist = session.createSQLQuery(basesql).list();
//		   List<?> baselist=baseService.getSQLList(basesql);
		   String itemsql="select " +
		   						"FCheckName," +//检验项目
		   						"FResult," +//检验结果
		   						"(select FName from lis_common_code where fcode=a.funit) as FUnit," +//单位
		   						"FReference," +//参考值
		   						"(select Fname from lis_common_code where fcode=a.FMethod) as FMethod " +//检验方法
		   				   "from  " +
		   				   		"Report_Result a " +
		   				   		"where FRPTID ='"+ID+"'";
//		   List<?> itemlist= baseService.getSQLList(itemsql);
		   List<?> itemlist = session.createSQLQuery(itemsql).list();
//		   List<?> itemlist = getCurrentSession().createSQLQuery(itemsql).list();
		   int page=itemlist.size()/rows;//定义会产生几页(改成从表里查)
		   ByteArrayOutputStream bos[] = new ByteArrayOutputStream[page+1];
		   try {	
			   //产生的待合并文件名（循环）
			   for (int current=0;current<=page;current++){
				PdfReader reader = new PdfReader(inpath);
				bos[current]= new ByteArrayOutputStream();
				PdfStamper ps = new PdfStamper(reader, bos[current]);
				//设置字体，大小可以直接在前台设
				BaseFont bf = BaseFont.createFont("c:/Windows/Fonts/STFANGSO.TTF",BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
				AcroFields s = ps.getAcroFields();		        
				s.addSubstitutionFont(bf);
				/**向模板中赋值*/
				//基本信息赋值
				PDFMethod.setBaseValue(baselist,s,ps,FMARGINST,FMARGINSD);
				//页码
				s.setField("page","第"+Integer.toString(current+1)+"页 -  共"+(page+1)+"页");
				//分页设值(每页rows个)——根据具体行数设定
				for (int i = 0; i <itemlist.size()-current*rows&&i<rows; i++) {
					Object[] obj = (Object[]) itemlist.get(current*rows+i);
					s.setField("NO"+Integer.toString(i+1),Integer.toString(i+1));//序号
					if(obj[0]!=null)s.setField("FCheckName"+Integer.toString(i+1), obj[0].toString());//项目名称
					if(obj[1]!=null)s.setField("FResult"+Integer.toString(i+1), obj[1].toString());//结果
					if(obj[2]!=null)s.setField("FUnit"+Integer.toString(i+1), obj[2].toString());//单位
					if(obj[3]!=null)s.setField("FReference"+Integer.toString(i+1), obj[3].toString());//参考值
					if(obj[4]!=null)s.setField("FMethod"+Integer.toString(i+1), obj[4].toString());//方法	
				}
				ps.setFormFlattening(true);
				/**向模板中插入二维码*/
//		        //设置一维码格式
				Image gif=PDFMethod.setOneCode(ps,ID);
		        gif.setAlignment(1);
		        PdfContentByte over = ps.getOverContent(1);
//		        gif.setAbsolutePosition(80,740);//控制一维码位置
		        gif.setAbsolutePosition(FMarginsL, FMarginsR);//float类型
		        gif.scaleAbsolute(100,50);//控制图片大小
		        over.addImage(gif);//将一维码设到Pdf当中		        
		        ps.close();	
	        	}
			   PDFMethod.mergeStream(outpath, page, bos);
			} catch (FileNotFoundException e) {
			e.printStackTrace();
			} catch (Exception e) {
			e.printStackTrace();
			}
	}

}
