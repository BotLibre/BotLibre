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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.SessionProxyBean;

@javax.servlet.annotation.WebServlet("/domain-icon-upload")
@SuppressWarnings("serial")
@MultipartConfig
public class DomainUploadServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		DomainBean bean = loginBean.getBean(DomainBean.class);
		uploadImage(request, bean);
		response.sendRedirect("domain?details=true" + proxy.proxyString());
	}
}
