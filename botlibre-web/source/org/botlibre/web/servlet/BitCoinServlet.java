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

import org.botlibre.web.bean.LoginBean;

/*
 * {"order":{"id":null,"created_at":null,"status":"completed","event":null,
 * "total_btc":{"cents":100000000.0,"currency_iso":"BTC"},
 * "total_native":{"cents":27650.0,"currency_iso":"CAD"},
 * "total_payout":{"cents":22564.0,"currency_iso":"USD"},
 * "custom":"123456789",
 * "receive_address":"1CnKPf6GNGwVyAYd3bhkckULc7ej1UkLSf",
 * "button":{"type":"buy_now","subscription":false,"repeat":null,"name":"Test Item","description":null,"id":null},
 * "transaction":{"id":"55368fcdea6cf3d428006201",
 * "hash":"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b","confirmations":0}}}
 */
@javax.servlet.annotation.WebServlet("/bcpayment")
@SuppressWarnings("serial")
public class BitCoinServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		String json = Utils.loadTextFile(request.getInputStream(), "utf-8", 1000000);
		
		LoginBean bean = getLoginBean(request, response);
		if (bean == null) {
			bean = new LoginBean();
			request.getSession().setAttribute("loginBean", bean);
		}
		bean.confirmBCPayment(json);
	}
}
