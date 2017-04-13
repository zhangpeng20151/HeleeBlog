package com.heleeos.blog.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.heleeos.blog.bean.Blog;
import com.heleeos.blog.bean.Manager;
import com.heleeos.blog.bean.Result;
import com.heleeos.blog.constant.BlogState;
import com.heleeos.blog.constant.ContentType;
import com.heleeos.blog.constant.SessionKey;
import com.heleeos.blog.service.BlogService;
import com.heleeos.blog.service.BlogTypeService;

@RestController
public class BlogController {
    
    @Autowired
    private BlogService blogService;
    
    @Autowired
    private BlogTypeService blogTypeService;

    @RequestMapping(value = "blog/list.html")
    public ModelAndView toBlogList() {
        ModelAndView modelAndView = new ModelAndView("blog/list");
        return modelAndView;
    }
    
    @RequestMapping(value = "blog/list.json")
    public Result getBlogType(HttpServletRequest request) {
        Result result = new Result();
        
        int page = NumberUtils.toInt(request.getParameter("page"), 1);
        int rows = NumberUtils.toInt(request.getParameter("rows"), 10);   
        int type = NumberUtils.toInt(request.getParameter("type"), 0);
        String tags = request.getParameter("tags");
        
        List<Blog> beans = blogService.gets(type, tags, false, page, rows);
        int count        = blogService.getCount(type, tags, false);
        
        result.putMessage("beans", beans);
        result.putMessage("page", page);
        result.putMessage("count", count);
        return result;
    }
    
    @RequestMapping(value = "blog/add.html")
    public ModelAndView toAdd(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("blog/add");
        int id = NumberUtils.toInt(request.getParameter("id"), 0);
        if(id != 0) {
            modelAndView.addObject("bean", blogService.get(id));
        }
        modelAndView.addObject("types", blogTypeService.gets());
        return modelAndView;
    }
    
    @RequestMapping(value = "blog/changeIndex.json")
    public Result changeIndex(HttpServletRequest request) {
        Result result = new Result();
        
        int id = NumberUtils.toInt(request.getParameter("id"), 0);
        int change = NumberUtils.toInt(request.getParameter("change"), 0);
        boolean flag = false;
        if(id > 0 && change != 0) {
             flag = blogService.changeIndex(id, change);
        } 
        
        result.setCode(flag ? 200 : 400);
        result.putInfo(flag ? "" : "修改失败");
        return result;
    }
    
    @RequestMapping(value = "blog/changeState.json")
    public Result changeState(HttpServletRequest request) {
        Result result = new Result();
        
        int id = NumberUtils.toInt(request.getParameter("id"), 0);
        String state = request.getParameter("state");
        
        boolean flag = false;
        if(id > 0) {
            flag = blogService.changeState(id, BlogState.of(state));
        }
        
        result.setCode(flag ? 200 : 400);
        result.putInfo(flag ? "" : "修改失败");
        return result;
    }
    
    @RequestMapping(value = "blog/update.json")
    public Result update(HttpServletRequest request) {
        Result result = new Result();
        
        Blog blog;
        int id = NumberUtils.toInt(request.getParameter("id"), -1);
        if(id == -1){
            blog = new Blog();
        }else{
            blog = blogService.get(id);
            if(blog == null) blog = new Blog();
        }
        
        String title = request.getParameter("title");
        String type = request.getParameter("type");
        String disp = request.getParameter("disp");
        String tags = request.getParameter("tags");
        String summary = request.getParameter("summary");
        String content = request.getParameter("content");
        int contentType = NumberUtils.toInt(request.getParameter("contentType"), 0);
        
        if(StringUtils.trimToNull(title) == null){
            result.setCode(400);
            result.putInfo("标题不能为空!");
            return result;
        }
        
        if(StringUtils.trimToNull(type) == null){
            result.setCode(400);
            result.putInfo("类型不能为空!");
            return result;
        }
        
        if(StringUtils.trimToNull(disp) == null){
            result.setCode(400);
            result.putInfo("显示URL不能为空!");
            return result;
        }
        
        if(StringUtils.trimToNull(summary) == null){
            result.setCode(400);
            result.putInfo("摘要不能为空!");
            return result;
        }
        
        if(StringUtils.trimToNull(content) == null){
            result.setCode(400);
            result.putInfo("内容不能为空!");
            return result;
        }
                
        blog.setTitle(title);
        blog.setType(type);
        blog.setDisp(disp);
        blog.setTags(tags);
        blog.setSummary(summary);
        blog.setContentType(ContentType.of(contentType).getType());
        blog.setContent(content);
        blog.setLasttime(new Date());
        
        Manager manager = (Manager) request.getSession().getAttribute(SessionKey.SESSION_MANAGER_KEY);
        blog.setManagerid(manager.getId());
        
        boolean flag = blogService.save(blog);

        result.setCode(flag ? 200 : 400);
        result.putInfo(flag ? "" : "提交失败");
        return result;
    }
}
