package bme.lis.common.action;

import bme.lis.basic.action.BaseAction;
import bme.lis.common.service.GeneratePDFService;

public class GeneratePDFAction extends BaseAction{
	private static final long serialVersionUID = 1L;
	private String id;
	private GeneratePDFService generatePdfService;
	public void GeneratePDF(){
		String picpath = req.getSession().getServletContext().getRealPath("/moban")+"\\";
		write(generatePdfService.generatePDF(id,picpath));
//		write(generatePdfService.generatePDF(id,"moban"));
	}
	public GeneratePDFService getGeneratePdfService() {
		return generatePdfService;
	}
	public void setGeneratePdfService(GeneratePDFService generatePdfService) {
		this.generatePdfService = generatePdfService;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
