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
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.botlibre.BotException;

import org.botlibre.web.Site;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.GraphicBean;
import org.botlibre.web.bean.LoginBean;

@javax.servlet.annotation.WebServlet("/graphic-import")
@SuppressWarnings("serial")
@MultipartConfig
public class GraphicImportServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		GraphicBean bean = loginBean.getBean(GraphicBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			byte[] bytes = null;
			Collection<Part> files = request.getParts();
			int count = 0;
			for (Part filePart : files) {
				if (!filePart.getName().equals("file")) {
					continue;
				}
				if (filePart != null) {
					InputStream stream = filePart.getInputStream();
					int max = Site.MAX_UPLOAD_SIZE * 2;
					if (loginBean.isSuper()) {
						max = max * 10;
					}
					bytes = BotBean.loadImageFile(stream, true, max);
				}
				if ((bytes == null) || (bytes.length == 0)) {
					continue;
				}
				bean.importGraphic(bytes);
				count++;
			}
			if (count == 0) {
				throw new BotException("Please select the graphic files to import");
			}
			response.sendRedirect("graphic?id=" + bean.getInstanceId());
		} catch (Throwable failed) {
			loginBean.error(failed);
			response.sendRedirect("browse-graphic.jsp");
		}
	}
}
