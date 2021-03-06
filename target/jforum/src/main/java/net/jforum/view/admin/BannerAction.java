/*
 * Copyright (c) 2003, 2004 Rafael Steil
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the
 * following disclaimer.
 * 2) Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor
 * the names of its contributors may be used to endorse
 * or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 *
 * Created on Mar 31, 2005 / 11:21:41 AM
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.view.admin;

import net.jforum.dao.BannerDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.entities.Banner;
import net.jforum.util.I18n;
import net.jforum.util.preferences.TemplateKeys;

/**
 * ViewHelper class for banner administration.
 *
 * @author Samuel Yung
 * @version $Id$
 */
public class BannerAction extends AdminCommand
{
	/**
	 * Listing
	 */
	public void list()
	{
		this.context.put("banners",
			DataAccessDriver.getInstance().newBannerDAO().selectAll());
		this.setTemplateName(TemplateKeys.BANNER_LIST);
	}

	/**
	 * Insert
	 */
	public void insert()
	{
		this.context.put("action", "insertSave");
		this.setTemplateName(TemplateKeys.BANNER_INSERT);
	}

	/**
	 * Saves a new banner
	 */
	public void insertSave()
	{
		final BannerDAO dao = DataAccessDriver.getInstance().newBannerDAO();

		dao.addNew(getBanner());

		this.list();
	}

	/**
	 * Edit a banner
	 */
	public void edit()
	{
		final int bannerId = this.request.getIntParameter("banner_id");
		final BannerDAO dao = DataAccessDriver.getInstance().newBannerDAO();

		this.context.put("banner", dao.selectById(bannerId));
		this.setTemplateName(TemplateKeys.BANNER_EDIT);
		this.context.put("action", "editSave");
	}

	/**
	 * Save information for an existing banner
	 */
	public void editSave() 
	{
		final int bannerId = this.request.getIntParameter("banner_id");

		final Banner banner = getBanner();
		banner.setId(bannerId);

		DataAccessDriver.getInstance().newBannerDAO().update(banner);

		this.list();
	}

	/**
	 * Delete a banner
	 */
	public void delete() 
	{
		final String bannerId = this.request.getParameter("banner_id");
		if(bannerId == null)
		{
			this.list();
			return;
		}

		final BannerDAO dao = DataAccessDriver.getInstance().newBannerDAO();

		final int id = Integer.parseInt(bannerId);
		if(dao.canDelete(id))
		{
			dao.delete(id);
		}
		else
		{
			this.context.put("errorMessage",
				I18n.getMessage(I18n.CANNOT_DELETE_BANNER));
		}

		this.list();
	}

	protected Banner getBanner()
	{
		final Banner banner = new Banner();
		banner.setComment(request.getParameter("comment"));
		banner.setActive(request.getIntParameter("active") == 1);
		banner.setType(Integer.parseInt(request.getParameter("type")));
		banner.setName(request.getParameter("name"));
		banner.setDescription(request.getParameter("description"));
		banner.setWidth(Integer.parseInt(request.getParameter("width")));
		banner.setHeight(Integer.parseInt(request.getParameter("height")));
		banner.setUrl(request.getParameter("url"));
		banner.setPlacement(Integer.parseInt(request.getParameter(
			"placement")));
		banner.setWeight(Integer.parseInt(request.getParameter("weight")));
		banner.setViews(Integer.parseInt(request.getParameter("views")));
		banner.setClicks(Integer.parseInt(request.getParameter("clicks")));

		return banner;
	}
}
