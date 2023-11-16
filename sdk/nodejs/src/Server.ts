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
import express from 'express'
import * as path from 'path';
import { Main } from './Main'

let main: Main

const app = express()
app.use(express.json())
app.use(express.static(path.join(__dirname, 'public')))


app.get('/', (req, res) => { //Loading home page.
    res.sendFile(path.join(__dirname, 'public', 'index.html'))
})

//TODO: MUST Implement router in another file.
//////////////////////////////////////////
app.post('/connect', async(req, res) => {
    if(main == null) {
        main = new Main({
            debug: true,
            adult: false,
            applicationId: req.body.applicationId,
            username: req.body.username,
            password: req.body.password
        })
    }
    let user = await main.connectUserAccount()
    res.setHeader('Content-Type', 'application/json')
    res.end(JSON.stringify(user));
})

app.post('/chat', async(req, res)=> {
    if(main == undefined || main == null) {
        res.json({message: "Must login first. Select API /connect to login"})
        return
    }
    let result = await main.sendChatMessage(req.body.message, req.body.botId)
    res.setHeader('Content-Type', 'application/json')
    res.end(JSON.stringify(result))
})
//////////////////////////////////////////




const PORT = 3000
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`)
})