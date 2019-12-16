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
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.botlibre.web.bean.AvatarBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;

@javax.servlet.annotation.WebServlet("/avatar-background-upload")
@SuppressWarnings("serial")
@MultipartConfig
public class AvatarBackgroundUploadServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		AvatarBean bean = loginBean.getBean(AvatarBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			String uploadURL = (String)request.getParameter("upload-url");
			if (uploadURL != null && uploadURL.trim().length() > 0) {
				URL url = BeanServlet.safeURL(uploadURL);
				URLConnection connection = null;
				try {
					connection = url.openConnection();
				} catch (Exception exception) {
					throw new BotException("Invalid URL");
				}
				String fileType = connection.getContentType();
				if (fileType.indexOf('+') != -1) {
					// Trim multiple content types.
					fileType = fileType.substring(0, fileType.indexOf('+'));
				}
				String fileName = uploadURL.substring(uploadURL.lastIndexOf('/') + 1);
				if (fileName.indexOf('=') != -1) {
					fileName = fileName.substring(fileName.lastIndexOf('=') + 1);
				}
				byte[] image = BotBean.loadImageFile(connection.getInputStream());
				bean.saveAvatarBackground(image, fileName, fileType);
			} else {
				byte[] image = null;
				Collection<Part> files = request.getParts();
				int count = 0;
				for (Part filePart : files) {
					if (!filePart.getName().equals("file")) {
						continue;
					}
					if (filePart != null) {
						InputStream stream = filePart.getInputStream();
						image = BotBean.loadImageFile(stream);
					}
					if ((image == null) || (image.length == 0)) {
						continue;
					}
					String fileName = getFileName(filePart);
					bean.saveAvatarBackground(image, fileName, filePart.getContentType());
					count++;
				}
				if (count == 0) {
					throw new BotException("Please select the image file to upload");
				}
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("avatar-editor.jsp");
	}
}
