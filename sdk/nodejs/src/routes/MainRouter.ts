/*
*  Copyright 2023 Paphus Solutions Inc.
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
*/

import express, { Request, Response, NextFunction } from 'express'
import cookieParser from 'cookie-parser'
export const mainRouter = express.Router()
import { Main } from '../Main'
import UserConfig from '../config/UserConfig'

mainRouter.use(cookieParser())

//Sessions are saved for each logged in user.
const seassions = new Map<string, Main>()

declare global {
    namespace Express {
        interface Request {
            userSession: Main
        }
    }
}
function sdkAuth(req: Request, res: Response, next: NextFunction) {
    const token = req.cookies.token
    const userSession = seassions.get(token)
    if (userSession) {
        req.userSession = userSession
        next()
    } else {
        res.json({ message: 'Unauthorized Access. Please check your credentials to login.' }).sendStatus(401)
    }
}

///////////
//ROUTERS//
///////////

mainRouter.post('/connect', async (req, res) => {
    let main = new Main({
        debug: true,
        adult: false,
        applicationId: req.body.applicationId,
        username: req.body.username,
        password: req.body.password
    })
    let user = await main.connectUserAccount()
    if (user instanceof UserConfig) {
        if (user && user.token) {
            seassions.set(user.token, main)
        }
        res.cookie('token', user.token, { httpOnly: true, secure: true, sameSite: 'strict' })
        res.setHeader('Content-Type', 'application/json')
        res.end(JSON.stringify(user))
    }else {
        res.end(JSON.stringify(user))
    }
})

mainRouter.post('/chat', sdkAuth, async (req: Request, res: Response) => {
    let result = await req.userSession.sendChatMessage(req.body.message, req.body.botId)
    res.setHeader('Content-Type', 'application/json')
    res.end(JSON.stringify(result))
})

mainRouter.post('/view-user', sdkAuth, async (req, res) => {
    let result = await req.userSession.fetchUser(req.body.username)
    res.setHeader('Content-Type', 'application/jsob')
    res.end(JSON.stringify(result))
})

