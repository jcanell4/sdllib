package org.elsquatrecaps.sdl.searcher;

import org.elsquatrecaps.sdl.model.FormatedFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elsquatrecaps.sdl.exception.ErrorGettingRemoteResource;
import org.elsquatrecaps.sdl.util.Utils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ArcaResource extends BvphTypeResource{
//    protected static final String[] ALL_SUPPORTED_FORMATS = {"pdf","jpg", "txt", "xml"};
    protected ArrayList<String> supportedFormats = new ArrayList<>();
    private String pdfTemporalUrl;

    public ArcaResource() {
    }
    
    public ArcaResource(String fragmentsFilter, String actionsFilter, String downloadJpg, String downloadPdf, String titleFilter, String editionDateBloc, String patterToExtractDateFromTitle) {
        this.fragmentsFilter = fragmentsFilter;
        this.actionsFilter = actionsFilter;
        this.jpgTemporalUrl = downloadJpg;
        this.pdfTemporalUrl = downloadPdf;
        this.titleFilter = titleFilter;
        this.editionDateBlocFilter = editionDateBloc;
        this.patterToExtractDateFromTitle = patterToExtractDateFromTitle;
    }
    
    public void updateFromElement(Element elem, String context, Map<String, String> cookies) {
        Element dlElement;
        List<Element> frags;
        try {
            dlElement = elem.parents().get(2);
            setPublicationId(dlElement.attr("id"));
            setTitle(dlElement.selectFirst(titleFilter).text());
            //            setPageId(elem.child(0).attr("id"));
            //            setPage(elem.selectFirst(pageFilter).text());
            setPageId(elem.child(1).attr("id"));
            setPage(elem.child(1).text());
            setEditionDate(getDateFromDbiOrTitle(dlElement.selectFirst(editionDateBlocFilter)));
            frags = new ArrayList<>(elem.select(fragmentsFilter));
            for (int i = 0; i < frags.size(); i++) {
                addFragment(frags.get(i).text());
            }
            String relativeUrl = elem.child(0).attr("href");
            String url = AbstractGetRemoteProcess.relativeToAbsoluteUrl(context, relativeUrl);
            Element toDonwloading = getToDownloading(url);
            Elements actions = toDonwloading.select(actionsFilter);
            if(actions.size()>0){
                ocrtextUrl = actions.get(0).child(0).attr("href");
                ocrtextUrl = AbstractGetRemoteProcess.relativeToAbsoluteUrl(context, ocrtextUrl + "&aceptar=Aceptar");
            }else{
                ocrtextUrl=null;
            }
            if(actions.size()>1){
                altoXmlUrl = actions.get(1).child(0).attr("href");
                altoXmlUrl = AbstractGetRemoteProcess.relativeToAbsoluteUrl(context, altoXmlUrl + "&aceptar=Aceptar");
            }else{
                altoXmlUrl=null;
            }
            //            jpgTemporalUrl = toDonwloading.select(saveJpgFilter).last().attr("href");
            jpgTemporalUrl = AbstractGetRemoteProcess.relativeToAbsoluteUrl(context, jpgTemporalUrl);
            pdfTemporalUrl = AbstractGetRemoteProcess.relativeToAbsoluteUrl(context, pdfTemporalUrl);
            this.updateSupportedFormats();
            
        } catch (Exception ex) {
            logger.error("Error carregant un registre: ".concat(ex.getMessage()));
            throw new ErrorGettingRemoteResource(ex);
        }
    }

    @Override
    protected boolean isFormatSupported(String format) {
        boolean ret = false;
        for (int i = 0; !ret && i < supportedFormats.size(); i++) {
            ret = supportedFormats.get(i).equalsIgnoreCase(format);
        }
        return ret;
    }

    @Override
    public String[] getSupportedFormats() {
        String[] ret = new String[supportedFormats.size()];
        return supportedFormats.toArray(ret);
    }
    
    private void updateSupportedFormats(){
        if(Utils.isCorrectContentType(jpgTemporalUrl, "jpeg")){
            if(ocrtextUrl!=null){
                this.supportedFormats.add("txt");
            }
            if(altoXmlUrl!=null){
                this.supportedFormats.add("xml");
            }
            this.supportedFormats.add("jpg");
        }
        if(Utils.isCorrectContentType(pdfTemporalUrl, "pdf")){
            this.supportedFormats.add("pdf");
        }
    }
    
    @Override
    protected FormatedFile getStrictFormatedFile(String format) {
        FormatedFile ret;
        if(format.equals("pdf")){
            ret = getFormatedFileInstance(pdfTemporalUrl, format);
        }else{
            ret = super.getStrictFormatedFile(format);
        }
        return ret;        
    }
    
    public String getFileName(){
        String fileName;
        if(isFormatSupported("pdf")){
            fileName = _getPdfFileName();
        }else{
            fileName = super.getFileName();
        }
        return fileName;
        
    }

    private String _getPdfFileName(){
        StringBuilder strBuffer = new StringBuilder();
        if(getEditionDate()!=null && !getEditionDate().isEmpty() && getEditionDate().matches("[0-9]{2}\\/[0-9]{2}\\/[0-9]{2,4}")){            
            String[] aDate = getEditionDate().split("\\/");
            strBuffer.append(aDate[2]);
            strBuffer.append("_");
            strBuffer.append(aDate[1]);
            strBuffer.append("_");
            strBuffer.append(aDate[0]);
        }else if(getEditionDate()!=null && !getEditionDate().isEmpty() && getEditionDate().matches("[0-9]{4}.+?[0-9]{1,2}")){            
            strBuffer.append(getEditionDate().substring(0, 4));
            strBuffer.append("_00_00");
        }else if(getEditionDate()!=null && !getEditionDate().isEmpty() && getEditionDate().matches("[0-9]{2}.+?[0-9]{4}")){            
            strBuffer.append(getEditionDate().substring(getEditionDate().length()-4, getEditionDate().length()));
            strBuffer.append("_00_00");
        }else if(getEditionDate()!=null && !getEditionDate().isEmpty() && getEditionDate().matches("[0-9]{1}.+?[0-9]{4}")){            
            strBuffer.append(getEditionDate().substring(getEditionDate().length()-4, getEditionDate().length()));
            strBuffer.append("_00_00");
        }else{
            strBuffer.append("0000_00_00");
        }
        strBuffer.append("_");
        strBuffer.append(this.getProcessDateResult());
        strBuffer.append("_");
        strBuffer.append(this.getPublicationId());
        strBuffer.append(Utils.buildNormalizedFilename(this.getTitle()));
        return strBuffer.toString().substring(0,Math.min(60, strBuffer.length()));        
    }
}
