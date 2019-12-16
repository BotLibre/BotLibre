/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.util.Utils;

import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.ChatWarBean;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.service.Stats;
import org.botlibre.web.bean.BotBean;

@javax.servlet.annotation.WebServlet({"/browse", "/BrowseServlet"})
@SuppressWarnings("serial")
public class BrowseServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = getLoginBean(request, response, true);
		try {
			if (loginBean == null) {
				return;
			}

			String domain = (String)request.getParameter("domain");
			if (domain != null) {
				DomainBean domainBean = loginBean.getBean(DomainBean.class);
				if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
					domainBean.validateInstance(domain);
				}
			}
			
			String affiliate = Utils.sanitize((String)request.getParameter("affiliate"));
			if (affiliate != null) {
				loginBean.setAffiliate(affiliate);
			}

			String download = (String)request.getParameter("download-desktop");
			if (download != null) {
				Stats.stats.desktopDownloads++;
				response.sendRedirect("download/botlibre-desktop.zip");
				return;
			}
			
			String browseType = Utils.sanitize((String)request.getParameter("browse-type"));
			if (browseType != null) {
				if (!browseType.equals("Twitter") && !browseType.equals("Email") && !browseType.equals("ChatWar")) {
					loginBean.setCategoryType(browseType);
				}
				String create = (String)request.getParameter("create");
				String browse = (String)request.getParameter("browse");
				String search = (String)request.getParameter("search");
				Page type = loginBean.getPageType();
				if (create != null) {
					type = Page.Create;
				} else if (browse != null) {
					type = Page.Browse;
				} else if (search != null) {
					type = Page.Search;
				}
				if (browseType.equals("Bot")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-instance.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("instance-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse.jsp").forward(request, response);
					}
				} else if (browseType.equals("Avatar")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-avatar.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("avatar-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-avatar.jsp").forward(request, response);
					}
				}  else if (browseType.equals("Graphic")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-graphic.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("graphic-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-graphic.jsp").forward(request, response);
					}
				} else if (browseType.equals("Script")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-script.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("script-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-script.jsp").forward(request, response);
					}
				} else if (browseType.equals("Channel")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-channel.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("channel-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-channel.jsp").forward(request, response);
					}
				} else if (browseType.equals("Forum")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-forum.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("forum-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-forum.jsp").forward(request, response);
					}
				} else if (browseType.equals("Analytic")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-analytic.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("analytic-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-analytic.jsp").forward(request, response);
					}
				} else if (browseType.equals("IssueTracker")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-issuetracker.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("issuetracker-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-issuetracker.jsp").forward(request, response);
					}
				} else if (browseType.equals("Domain")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-domain.jsp").forward(request, response);
					} else if (type == Page.Search) {
						request.getRequestDispatcher("domain-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("domains.jsp").forward(request, response);
					}
				} else if (browseType.equals("User")) {
					if (type == Page.Create) {
						request.getRequestDispatcher("create-user.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-user.jsp").forward(request, response);
					}
				} else if (browseType.equals("Desktop")) {
					request.getRequestDispatcher("botlibre-desktop.jsp").forward(request, response);
				} else if (browseType.equals("Corporate") || browseType.equals("Enterprise")) {
					response.sendRedirect("enterprise-bot-platform.jsp");
				} else if (browseType.equals("Cloud")) {
					response.sendRedirect("cloud-bot-platform.jsp");
				} else if (browseType.equals("ChatWar")) {
					ChatWarBean bean = loginBean.getBean(ChatWarBean.class);
					String bot1 = (String)request.getParameter("bot1");
					if (bot1 != null) {
						bean.getInstance1().validateInstance(bot1);
					}
					String bot2 = (String)request.getParameter("bot2");
					if (bot2 != null) {
						bean.getInstance1().validateInstance(bot2);
					}
					String topic = (String)request.getParameter("topic");
					if (topic != null) {
						bean.setTopic(topic);
					}
					request.getRequestDispatcher("chatwar-start.jsp").forward(request, response);
				}
				return;
			}

			// Old code, backward compat
			String viewUser = (String)request.getParameter("view-user");
			if (viewUser != null) {
				if (!loginBean.viewUser(viewUser)) {
					request.getRequestDispatcher("user.jsp").forward(request, response);
					return;
				}
				request.getRequestDispatcher("user.jsp").forward(request, response);
				return;
			}

			BotBean botBean = loginBean.getBotBean();
			loginBean.setActiveBean(botBean);
			String id = (String)request.getParameter("id");
			if (id == null) {
				id = (String)request.getParameter("browse");
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");
			
			String search = (String)request.getParameter("search-instance");
			if (search != null) {
				botBean.resetSearch();
				botBean.setCategoryFilter(botBean.getCategoryString());
				request.getRequestDispatcher("instance-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				botBean.browseCategory(category);
				request.getRequestDispatcher("browse.jsp").forward(request, response);
				return;
			}
			String create = (String)request.getParameter("create-instance");
			if (create != null) {
				request.getRequestDispatcher("create-instance.jsp").forward(request, response);
				return;
			}
			String createLink = (String)request.getParameter("create-link");
			if (createLink != null) {
				request.getRequestDispatcher("create-instance-link.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Bot");
				loginBean.setActiveBean(botBean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Bot");
				loginBean.setActiveBean(botBean);
				loginBean.setCategory(botBean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.setCategoryType("Bot");
				loginBean.setActiveBean(botBean);
				loginBean.setCategory(botBean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse.jsp");
				return;
			}
			
			if (id != null) {
				if (!botBean.validateInstance(id)) {
					request.getRequestDispatcher("instance.jsp").forward(request, response);
					return;
				}
				request.getRequestDispatcher("instance.jsp").forward(request, response);
				return;
			} else if (page != null) {
				botBean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("instance-search.jsp").forward(request, response);
				return;
			} else if (userFilter != null) {
				botBean.resetSearch();
				botBean.setUserFilter(Utils.sanitize(userFilter));
				botBean.setInstanceFilter(InstanceFilter.Personal);
				request.getRequestDispatcher("instance-search.jsp").forward(request, response);
				return;
			} else {
				setSearchFields(request, botBean);
				request.getRequestDispatcher("instance-search.jsp").forward(request, response);
				return;
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("instance-search.jsp").forward(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
