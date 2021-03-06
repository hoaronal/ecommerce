package com.framgia.controller.admin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.framgia.bean.CategoryInfo;
import com.framgia.bean.ProductInfo;
import com.framgia.constant.Paginate;
import com.framgia.validator.CategoryValidator;

@Controller("admin/category")
@RequestMapping(value = "admin/categories")
public class CategoriesController extends AdminController {

	@Autowired
	private CategoryValidator categoryValidator;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView index(@RequestParam(value = "entries", required = false) String entries) {
		ModelAndView model = new ModelAndView("adminCategories");
		if (StringUtils.isNotEmpty(entries)) {
			if (entries.equals("all"))
				model.addObject("categories",
				    categoryService.getCategories(0, 0, Order.desc("createdAt")));
			else
				model.addObject("categories", categoryService.getCategories(0,
				    Integer.parseInt(entries), Order.desc("createdAt")));
		} else
			model.addObject("categories", categoryService.getCategories(0,
			    Paginate.ADMIN_OBJECT_LIMIT, Order.desc("createdAt")));
		return model;
	}

	@RequestMapping(value = "new", method = RequestMethod.GET)
	public ModelAndView getNew() {
		ModelAndView model = new ModelAndView("adminNewCategory");
		List<CategoryInfo> categories = categoryService.getObjects();
		model.addObject("categories", categories);
		model.addObject("category", new CategoryInfo());
		return model;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView create(@ModelAttribute("category") CategoryInfo categoryInfo,
	    BindingResult result) throws JsonProcessingException {
		ModelAndView model = new ModelAndView();
		categoryValidator.validate(categoryInfo, result);
		if (!result.hasErrors() && categoryService.createCategory(categoryInfo)) {
			model.setViewName("redirect");
			model.addObject("url",
			    request.getContextPath() + "/admin/categories/" + categoryInfo.getId());
		} else {
			model.setViewName("inputError");
			model.addObject("errors", convertErrorsToHashMap(result.getFieldErrors()));
		}

		return model;
	}

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public ModelAndView show(@PathVariable Integer id,
	    @RequestParam(value = "entries", required = false) String entries) {
		CategoryInfo category = categoryService.findById(id, false);
		ModelAndView model = new ModelAndView("adminCategory");
		if (category != null) {
			List<ProductInfo> products = null;
			if (StringUtils.isNotEmpty(entries)) {
				if (entries.equals("all"))
					products = productService.getProducts(category.getId(), null, 0, null);
				else
					products = productService.getProducts(category.getId(), null,
					    Integer.parseInt(entries), null);
			} else {
				products = productService.getProducts(category.getId(), null,
				    Paginate.ADMIN_OBJECT_LIMIT, null);
			}
			model.addObject("category", category);
			model.addObject("products", products);
		} else {
			model.setViewName("admin404");
		}
		return model;
	}

	@RequestMapping(value = "{id}/edit", method = RequestMethod.GET)
	public ModelAndView edit(@PathVariable("id") Integer id) {
		ModelAndView model = new ModelAndView("adminEditCategory");
		CategoryInfo categoryInfo = categoryService.findById(id, true);
		if (categoryInfo != null) {
			model.addObject("categories",
			    categoryService.getCategoriesWithForNew(categoryInfo.getId()));
			model.addObject("category", categoryInfo);
		} else
			model.setViewName("admin404");
		return model;
	}

	@RequestMapping(value = "{id}", method = RequestMethod.POST)
	public ModelAndView update(@PathVariable("id") Integer id,
	    @ModelAttribute("category") CategoryInfo categoryInfo, BindingResult result)
	    throws JsonProcessingException {
		CategoryInfo oldCategory = categoryService.findById(id, true);
		ModelAndView model = new ModelAndView();
		if (oldCategory != null) {
			categoryValidator.validateUpdate(oldCategory, categoryInfo, result);
			if (!result.hasErrors() && categoryService.updateCategory(oldCategory, categoryInfo)) {
				model.setViewName("redirect");
				model.addObject("url",
				    request.getContextPath() + "/admin/categories/" + categoryInfo.getId());
			} else {
				model.setViewName("inputError");
				model.addObject("errors", convertErrorsToHashMap(result.getFieldErrors()));
			}
		}
		return model;
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public ModelAndView delete(@PathVariable("id") Integer id) {
		ModelAndView model = new ModelAndView();
		CategoryInfo categoryInfo = categoryService.findById(id, true);
		if (categoryInfo != null) {
			categoryService.delete(categoryInfo);
			model.setViewName("redirect:/admin/categories");
		} else
			model.setViewName("admin404");
		return model;
	}
}
