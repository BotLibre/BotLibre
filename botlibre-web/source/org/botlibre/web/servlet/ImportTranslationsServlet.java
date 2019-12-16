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

import org.botlibre.web.bean.LoginBean;

@javax.servlet.annotation.WebServlet("/upload-translations")
@SuppressWarnings("serial")
@MultipartConfig
public class ImportTranslationsServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			Collection<Part> files = request.getParts();
			int count = 0;
			for (Part filePart : files) {
				if (!filePart.getName().equals("file")) {
					continue;
				}
				if (filePart != null) {
					InputStream stream = filePart.getInputStream();
					loginBean.importTranslations(stream);
					count++;
				}
			}
			if (count == 0) {
				throw new BotException("Please select the XML file");
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("translation.jsp");		
	}
}
