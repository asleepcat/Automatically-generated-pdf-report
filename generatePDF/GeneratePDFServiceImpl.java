package bme.lis.common.service.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import bme.lis.common.service.GeneratePDFService;
import bme.lis.common.util.PDFMethod;
import bme.lis.common.util.PDFtemplate.biochemistry;
/**
 * 
 * @author 张昊       功能：将值，一维码设入PDF并且进行一次性分页并且抽象功能进行
 *
 */
public class GeneratePDFServiceImpl extends HibernateDaoSupport implements
GeneratePDFService{
//	public HttpServletRequest req = ServletActionContext.getRequest();
	public static int rows;//定义产生的页数
	public static Float FMarginsL;//二维码的X坐标 
	public static Float FMarginsR;//二维码的Y坐标 
	public static Float FMARGINST;//急标识的X坐标 
	public static Float FMARGINSD;//急标识的Y坐标 
	// 获得当前Session
	public Session getCurrentSession() {
		return this.getHibernateTemplate().getSessionFactory()
				.getCurrentSession();
	}	
	//根据传进来的ID判断是哪种模版
	public String generatePDF(String ID,String picpath) {
		String mobansql="select " +
						"FURL," +//报告单文件（模板文件）0
						"a.FDBMode," +//数据处理类型 1
						"a.FPageRows," +//每页的行数 2
						"a.FMarginsL," +//二维码的X坐标 3
						"a.FMarginsR," +//二维码的Y坐标 4
						"a.FMARGINST," +//急标识的X坐标 5
						"a.FMARGINSD " +//急标识的Y坐标 6
					"from " +
						"BaseReport_Format a," +
						"BaseReport_WorkStation b," +
						"Report_Bill c " +
					"where " +
						"c.frptid='"+ID+"' and " +
						"c.FWKSID  = b.fwksid and " +
						"b.FRPTType=a.FRPTID";
		List<?> mobanlist = getCurrentSession().createSQLQuery(mobansql).list();
		Object[] obj = (Object[]) mobanlist.get(0);
		rows=Integer.parseInt(obj[2].toString());
		FMarginsL=Float.valueOf(obj[3].toString());
		FMarginsR=Float.valueOf(obj[4].toString());
		FMARGINST=Float.valueOf(obj[5].toString());
		FMARGINSD=Float.valueOf(obj[6].toString());
		//****************************************************
		//根据数据处理类型决定使用哪种处理方式
		/**
		 * SH---生化处理（还会有很多的处理方式）
		 */
		String PDFname=obj[0].toString();
		if (obj[1].toString().equals("SH")){ 
			new biochemistry().setSH(ID,"d:/moban/"+PDFname+"", "d:/biochemistry化验单.pdf",this,getCurrentSession());
		}
		//****************************************************
//		String picpath = req.getSession().getServletContext().getRealPath("/moban")+"\\";
		String PicTurename=PDFMethod.changePdfToImg("d:/biochemistry化验单.pdf",picpath);
		return PicTurename;	//返回的路径
	}
}
