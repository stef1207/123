package bjtu.shhd.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import bjtu.shhd.exception.UserRuntimeException;
import bjtu.shhd.pojo.Document;
import bjtu.shhd.pojo.Problem;
import bjtu.shhd.pojo.api.PageBean;
import bjtu.shhd.pojo.api.ResultBean;
import bjtu.shhd.service.DocumentService;
import bjtu.shhd.service.PojoCreator;
import bjtu.shhd.utils.ConversionUtils;
import bjtu.shhd.utils.DateUtils;
import bjtu.shhd.utils.JsonUtils;
import bjtu.shhd.utils.RequestUtils;

@Controller
@RequestMapping("/api/doc")
public class DocApiController {
	//试试
    
    @Autowired
    private DocumentService documentService;

    @Autowired
    private PojoCreator pojoCreator;
    
    @RequestMapping("/create")
    @ResponseBody
    public ResultBean<Long> create(HttpServletRequest request, HttpServletResponse reponse) throws Exception {
        Document doc = new Document();
        setValue(request, doc);
        documentService.insert(doc, getAttach(request), getOrgIds(request));
        
        ResultBean<Long> result = new ResultBean<Long>();
        result.setSuccess(true);
        result.setData(doc.getId());
        return result;
    }
    
    @RequestMapping("/modify")
    @ResponseBody
    public ResultBean<Long> modify(HttpServletRequest request, HttpServletResponse reponse) throws Exception {
        Long id = ConversionUtils.getLong(request.getParameter("id"));
        Document doc = pojoCreator.newPojo(Document.class);
        doc.setId(id);
        setValue(request, doc);
        
        documentService.update(doc, getAttach(request), getOrgIds(request));
        
        ResultBean<Long> result = new ResultBean<Long>();
        result.setSuccess(true);
        result.setData(doc.getId());
        return result;
    }
    
    private void setValue(HttpServletRequest request, Document doc) {
        doc.setCaption(request.getParameter("caption"));
        doc.setCode(request.getParameter("code"));
        doc.setReleaseDept(request.getParameter("releaseDept"));
        doc.setDepart(request.getParameter("depart"));
        doc.setKeyWords(request.getParameter("keyWords"));
        doc.setContent(request.getParameter("content"));
        try {
            doc.setReleaseDate(DateUtils.parseDate(request.getParameter("releaseDate"), "yyyy-MM-dd"));
        } catch (ParseException e) {
            throw new UserRuntimeException("发文日期格式不正确", "100");
        }
    }
    
    /**
     * 生成附件内容。
     * @param request
     * @return
     * @throws Exception
     */
    private List<Map<String, Object>> getAttach(HttpServletRequest request) throws Exception {
        String str = request.getParameter("attach");
        if (str == null || str.length() == 0) {
            return null;
        }
        return (List<Map<String, Object>>)JsonUtils.jsonToObject(str, ArrayList.class);
    }
    
    /**
     * 生成权限内容。
     * @param request
     * @return
     * @throws Exception
     */
    private Long[] getOrgIds(HttpServletRequest request) {
        String[] arr = request.getParameterValues("orgIds[]");
        if (arr == null || arr.length <= 0) {
            return null;
        }
        
        Long[] ids = new Long[arr.length];
        int i = 0;
        for (String str : arr) {
            ids[i++] = ConversionUtils.getLong(str);
        }
        
        return ids;
    }
    
    /**
     * 删除。
     * @param request
     * @return
     */
    @RequestMapping("/del")
    @ResponseBody
    public ResultBean<Object> del(HttpServletRequest request) {
        Long[] ids = getIds(request);
        documentService.del(ids);
        
        ResultBean<Object> result = new ResultBean<Object>();
        result.setSuccess(true);
        return result;
    }
    
    private Long[] getIds(HttpServletRequest request) {
        String[] arr = request.getParameterValues("ids[]");
        if (arr == null || arr.length <= 0) {
            return null;
        }
        
        Long[] ids = new Long[arr.length];
        int i = 0;
        for (String str : arr) {
            ids[i++] = ConversionUtils.getLong(str);
        }
        
        return ids;
    }
    
    @RequestMapping("/findById")
    @ResponseBody
    public ResultBean<Document> findById(HttpServletRequest request) {
        Long id = ConversionUtils.getLong(request.getParameter("id"));
        Document doc = documentService.findByIdForAllInfo(id);
        
        ResultBean<Document> result = new ResultBean<Document>();
        result.setSuccess(true);
        result.setData(doc);
        return result;
    }
    
    /**
     * 查询。
     * @param request
     * @return
     */
    @RequestMapping("/find")
    @ResponseBody
    public ResultBean<PageBean<Document>> find(HttpServletRequest request) {
        Map<String, Object> param = genFindParam(request);
        
        PageBean<Document> page = RequestUtils.genPageBean(request, Document.class);
        
        documentService.find(param, page);
        
        ResultBean<PageBean<Document>> result = new ResultBean<PageBean<Document>>();
        result.setSuccess(true);
        result.setData(page);
        
        return result;
    }
    
    /**
     * 查询。
     * @param request
     * @return
     */
    @RequestMapping("/query")
    @ResponseBody
    public ResultBean<PageBean<Document>> query(HttpServletRequest request) {
        Map<String, Object> param = genFindParam(request);
        
        PageBean<Document> page = RequestUtils.genPageBean(request, Document.class);
        
        documentService.query(param, page);
        
        ResultBean<PageBean<Document>> result = new ResultBean<PageBean<Document>>();
        result.setSuccess(true);
        result.setData(page);
        
        return result;
    }
    
    private Map<String, Object> genFindParam(HttpServletRequest request) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("caption", request.getParameter("caption"));
        param.put("code", request.getParameter("code"));
        param.put("releaseDept", request.getParameter("releaseDept"));
        param.put("depart", request.getParameter("depart"));
        param.put("keyWords", request.getParameter("keyWords"));
        try {
            String str = request.getParameter("startDate");
            if (str != null && str.length() > 0) {
                param.put("releaseDateGte", DateUtils.parseDate(str, "yyyy-MM-dd"));
            }
            str = request.getParameter("endDate");
            if (str != null && str.length() > 0) {
                param.put("releaseDateLt", DateUtils.addDay(DateUtils.parseDate(str, "yyyy-MM-dd"), 1));
            }
        } catch (ParseException e) {
            throw new UserRuntimeException("发文日期格式不正确", "100");
        }
        
        RequestUtils.genOrderby(request, param);
        
        return param;
    }
}
