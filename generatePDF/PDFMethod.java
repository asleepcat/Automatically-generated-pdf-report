package bme.lis.common.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.jbarcode.JBarcode;
import org.jbarcode.encode.Code128Encoder;
import org.jbarcode.encode.EAN13Encoder;
import org.jbarcode.encode.InvalidAtributeException;
import org.jbarcode.paint.BaseLineTextPainter;
import org.jbarcode.paint.EAN13TextPainter;
import org.jbarcode.paint.WideRatioCodedPainter;
import org.jbarcode.paint.WidthCodedPainter;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

@SuppressWarnings("restriction")
/**
 * 
 * @author 张昊           
 * 日期：2014.07.19
 * 功能：主要是PDF用到的操作方法
 * （PDF转成图片）
 *
 */
public class PDFMethod {
	/**
	 * 查询模板信息
	 */
	public static String templateinfo(String ID) {
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
		return mobansql;
		
	}
	/**
	 * 查询基本信息
	 * 
	 * 右侧备注具体来源字段  （NO,名字）。
	 */
	@SuppressWarnings("rawtypes")
	public static String queryBaseValue(String ID){
		   String basesql="select " +
				   				"((select FPARAMVALUE from sys_parameter where fparamid='HospitalName')||' '||(select a.FRPTTITLE from basereport_workstation a, report_bill b where a.FWKSID=b.fwksid and b.frptid='"+ID+"')) as HospitalName," +//0.标题之报告单医院名称   sys_parameter  关联字段Fparamid=HospitalName
						   		"b.FName," +//1.姓名  Report_Bill
						   		"b.FRPTID," +//2.报告单ID Report_Bill
						   		"(select b.Fname from lis_common_code b,report_bill a where a.fsex= b.fcode and a.frptid='"+ID+"'and b.fparentid='142') as fsex," +//3.性别  
						   		"(select a.Fname from sys_dept a,report_bill b where a.fcode=b.fdept and b.frptid='"+ID+"') as Fdept," +//4.部门
						   		"b.FAge," +//5.年龄
						   		"(select a.Fname from lis_common_code a, report_bill b where a.fcode=b.FSAMType and a.fparentid='102' and b.frptid='"+ID+"') as FSAMType," +//6.样品类型
						   		"(select a.Fname from lis_common_code a, report_bill b where a.fcode=b.FPatientSort and a.fparentid='235' and b.frptid='"+ID+"') as FPatientSort," +//7.患者类型
						   		"b.FBedNo," +//8.床号
						   		"Fquality," +//9.样品状态？
						   		"FClinical," +//10.临床诊断			   		
						   		"(select a.FresultID from Report_CheckRecord a, report_bill b where a.frptid=b.frptid and b.frptid='"+ID+"') as FresultID," +//11.检测编号
						   		"(select p.fname||' '||to_char(b.FAPPDT,'yyyy-mm-dd') as FAPPLICANTS from sys_person p,report_bill b where p.fpersonid=b.fapplicants and b.frptid ='"+ID+"')," +//12.申请者+申请时间
						   		"(select FWKSNAME  as finsraname from basereport_workstation a, report_bill b where a.fwksid= b.fwksid and b.frptid='"+ID+"')," +//13.仪器??finsraname
					            "(select p.fname||' '||to_char(b.FSAMPLINGDT,'yyyy-mm-dd') as FSAMPLER from sys_person p,report_bill b where p.fpersonid=b.FSampler and b.frptid ='"+ID+"')," +//14.采样者+采样时间						   		
						        "(select p.fname||' '||to_char(b.FCheckDT,'yyyy-mm-dd') as FCheck from sys_person p,report_bill b where p.fpersonid=b.FCheck and b.frptid ='"+ID+"')," +//15.检验者+检验时间							   		
							    "(select p.fname||' '||to_char(b.FRECEIVEDT,'yyyy-mm-dd') as FRECEIVE from sys_person p,report_bill b where p.fpersonid=b.FReceive and b.frptid ='"+ID+"')," +//16.收样者+收样时间								   		
							    "(select p.fname||' '||to_char(b.FVERIFYDT,'yyyy-mm-dd') as FVERIFY from sys_person p,report_bill b where p.fpersonid=b.FVerify and b.frptid ='"+ID+"')," +//17.审核者+审核时间		
						   		"(select FPARAMVALUE from sys_parameter where FPARAMID='ReportRemark') as ReportRemark,"+//18.左边固定的报告单备注
						   		"b.FRemark," +//19.右边的备注
						   		"FEmergency "+//20是否急症
						   		
						  "from " +
						   		"report_bill b " +
						  "where " +
						   		"b.FRPTID='"+ID+"'";
//		   List<?> baselist=baseService.getSQLList(basesql);
//		   List<?> baselist = getCurrentSession().createSQLQuery(basesql).list();
		   return basesql;		   
	}
	/**
	 *  
	 * 功能：将基本信息(包括急症图片)设入PDF(顺序要进行对应)
	 * 传入的参数：基础信息的list(List),文本编辑域(AcroFields s)
	 * 0.医院名称（HospitalName）				10.临床诊断（FClinical）
	 * 1.姓名（FName）							11.检测编号（FresultID）
	 * 2.报告单ID（FRPTID）						12.申请者+申请时间（FApplicants）
	 * 3.性别（FSex）					        13.仪器（FInsraname）
	 * 4.部门（FDept）						    14.采样者+采样时间（FSampler）
	 * 5.年龄（FAge）						    15.检验者+检验时间（FCheck）
	 * 6.样品类型（FSAMType）					16.收样者+收样时间（FReceive）
	 * 7.患者类型（FPatientSort）				17.审核者+审核时间（FVerify）
	 * 8.床号（FBedNo）			            18.左边固定的报告单备注（ReportRemark）
	 * 9.样品状态（FQuality）				    19.右边的备注（FRemark）
	 										20.是否急症（1,0）
	 * 对于下面的字符数组循环一定要按照上面的数据顺序进行设值，并且要和PDF当中的文本域的名称相符
	 * @return 
	 * @throws DocumentException 
	 * @throws IOException 
	 * 
	 */
	public static void setBaseValue(List<?> baselist,AcroFields s,PdfStamper ps,float FMARGINST,float FMARGINSD) throws IOException, DocumentException{
		String name[]={"HospitalName","FName","FRPTID","FSex","FDept","FAge","FSAMType","FPatientSort","FBedNo","FQuality","FClinical","FresultID","FApplicants","FInsraname","FSampler","FCheck","FReceive","FVerify","ReportRemark","FRemark"};//定义字符串，通过循环简化
		for(int i=0; i<=19;i++){
			if(((Object[])baselist.get(0))[i]!= null){
					s.setField(name[i], ((Object[])baselist.get(0))[i].toString());
			}	
		}
		//是否是急症
		System.out.println(((Object[])baselist.get(0))[20]);
		if(((Object[])baselist.get(0))[20].toString().equals("1")&&((Object[])baselist.get(0))[20]!=null){
			   PdfContentByte over = ps.getOverContent(1);
			   Image img1 = Image.getInstance("D:/图片/hurry.PNG");//选择图片
		        img1.setAbsolutePosition(FMARGINST,FMARGINSD);//控制位置
		        img1.scaleAbsolute(20,18);//控制图片大小
		        over.addImage(img1);//将一维码设到Pdf当中
		}
	}

	/**
	 *  
	 * 功能：将一维码设入PDF(顺序要进行对应)
	 * 传入的参数：PDF编辑域（PdfStamper ps）
	 * @throws InvalidAtributeException 
	 * @throws IOException 
	 * @throws DocumentException 
	 * 
	 */
	public static Image setOneCode (PdfStamper ps,String ID) throws InvalidAtributeException, IOException, DocumentException{
		  JBarcode localJBarcode = new JBarcode(EAN13Encoder.getInstance(), WidthCodedPainter.getInstance(), EAN13TextPainter.getInstance());  	      
	    	String str = ID;  
//			BufferedImage localBufferedImage = localJBarcode.createBarcode(str);
			localJBarcode.setEncoder(Code128Encoder.getInstance());//采用128编码
		    localJBarcode.setPainter(WideRatioCodedPainter.getInstance());  
		    localJBarcode.setTextPainter(BaseLineTextPainter.getInstance());  
		    localJBarcode.setShowCheckDigit(false);
		    str = ""+str+"";  
//		    str = "JBARCODE-128";
		    //	更换类型用下面一句（暂注）
		    BufferedImage localBufferedImage1 = localJBarcode.createBarcode(str);
		    Image gif = Image.getInstance(localBufferedImage1, null, false);	
		    return gif;
	}
	/**
	 * 功能：将输出流进行合并
	 * 传入的参数：输出路径(outpath),页数(page),输出流数组(bos[])
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void mergeStream(String outpath,int page,ByteArrayOutputStream bos[]) throws DocumentException, IOException{
	    FileOutputStream fos = new FileOutputStream(outpath);				
        Document doc = new Document();   
        PdfCopy pdfCopy = new PdfCopy(doc, fos);   
        doc.open();  
        PdfImportedPage impPage = null;   
        /**取出之前保存的每页内容,合并几个输出流*/  
        for (int i = 0; i < page+1; i++) {   
            impPage = pdfCopy.getImportedPage(new PdfReader(bos[i]  
                    .toByteArray()), 1);   
            pdfCopy.addPage(impPage);   
        } 
        doc.close();//当文件拷贝  记得关闭doc  
	}
	/**
	 * 功能：将pdf转化成图片
	 * 传入的参数：PDF的输入路径(PdfIn),图片输出路径(PicOut)
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static String changePdfToImg(String PdfIn,String PicOut) {//PicOut="D:\\"
		String outPi=null;
		StringBuilder outP= new StringBuilder();//定义输出的字符串
		try {
			File file = new File(PdfIn);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			FileChannel channel = raf.getChannel();
			MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
					0, channel.size());
			PDFFile pdffile = new PDFFile(buf);
			for (int i = 1; i <= pdffile.getNumPages(); i++) {
				PDFPage page = pdffile.getPage(i);
				Rectangle rect = new Rectangle(0, 0, ((int) page.getBBox()
						.getWidth()), ((int) page.getBBox().getHeight()));
				int n = 7;
				/** 图片清晰度（n>0且n<7）【pdf放大参数】 */
				java.awt.Image img = page.getImage(rect.width * n, rect.height * n,
						rect, /** 放大pdf到n倍，创建图片。 */
						null, /** null for the ImageObserver */
						true, /** fill background with white */
						true /** block until drawing is done */
				);
				BufferedImage tag = new BufferedImage(rect.width * n,
						rect.height * n, BufferedImage.TYPE_INT_RGB);
				tag.getGraphics().drawImage(img, 0, 0, rect.width * n,
						rect.height * n, null);
				FileOutputStream out = new FileOutputStream(PicOut+ i+".jpg");
				outP.append("moban/" + i+".jpg,");//此处的路径为服务器的指定的存储模板的文件夹名字。以后可以采用截取上面的动态路径的方式获得。
				
				/** 输出到文件流 */

				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
				JPEGEncodeParam param2 = encoder.getDefaultJPEGEncodeParam(tag);
				param2.setQuality(0.1f, true);
				/** 1f~0.01f是提高生成的图片质量 */
				encoder.setJPEGEncodeParam(param2);
				encoder.encode(tag);
				/** JPEG编码 */
				out.close();
			}
			//**********************************************
			outP.delete(outP.length()-1,outP.length());	//
			outPi=outP.toString();//把图片的路径输出（"","",""的格式输出）
//			PrintWriter out;//
//			
//			out = res.getWriter();//
//			out.write(outP.toString());//
//			out.flush();//
//			out.close();//
//			channel.close();//
			//**********************************************
			raf.close();
			unmap(buf);
			/** 如果要在转图片之后删除pdf，就必须要这个关闭流和清空缓冲的方法 */
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outPi;
	}
	//转化图片里面需要的方法
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void unmap(final Object buffer) {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					Method getCleanerMethod = buffer.getClass().getMethod(
							"cleaner", new Class[0]);
					getCleanerMethod.setAccessible(true);
					sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod
							.invoke(buffer, new Object[0]);
					cleaner.clean();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}




}
