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
import multer from 'multer'
import { Main } from '../Main'
import UserConfig from '../config/UserConfig'
import ForumConfig from '../config/ForumConfig'
import { ForumPostConfig } from '../config/ForumPostConfig'


const storage = multer.memoryStorage()
const upload = multer({ storage: storage });

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

function post(path: string, callback: (req: Request, res: Response) => void) {
    mainRouter.post(path, sdkAuth, async (req, res) => {
        res.setHeader('Content-Type', 'application/json')
        callback(req, res);
    })
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
    } else {
        res.end(JSON.stringify(user))
    }
})


post('/chat', async function (req, res) {
    let result = await req.userSession.sendChatMessage(req.body.message, req.body.botId)
    res.end(JSON.stringify(result))
})

post('/view-user', async (req, res) => {
    let result = await req.userSession.fetchUser(req.body.username)
    res.end(JSON.stringify(result))
})

post('/check-forum-post', async (req, res) => {
    let result = await req.userSession.fetchForumPost(req.body.forumId)
    res.end(JSON.stringify(result))
})

post('/create-user', async (req, res) => {
    let result = await req.userSession.createUser({
        user: req.body.user, password: req.body.password, hint: req.body.hint, name: req.body.name, email: req.body.email,
        website: req.body.website, bio: req.body.bio, showName: req.body.showName
    })
    res.end(JSON.stringify(result))
})

post('/get-forum-admins', async (req, res) => {
    let result = await req.userSession.getAdminsForum({
        forumId: req.body.forumId
    })
    res.end(JSON.stringify(result))
})

post('/get-forum-users', async (req, res) => {
    let result = await req.userSession.getUsersForum({
        forumId: req.body.forumId
    })
    res.end(JSON.stringify(result))
})

post('/get-forum-posts', async (req, res) => {
    let result = await req.userSession.getForumPosts({
        type: req.body.type,
        typeFilter: req.body.typeFilter,
        sort: req.body.sort
    })
    return res.end(JSON.stringify(result))
})

post('/get-categories', async (req, res) => {
    let result = await req.userSession.getCategories({
        type: req.body.type
    })
    return res.end(JSON.stringify(result))
})

post('/get-tags', async (req, res) => {
    let result = await req.userSession.getTags({
        type: req.body.type
    })
    return res.end(JSON.stringify(result))
})

post('/get-all-templates', async (req, res) => {
    let result = await req.userSession.getAllTemplates()
    return res.end(JSON.stringify(result))
})

post('/get-channel-bot-mode', async (req, res) => {
    let result = await req.userSession.getChannelBotMode({
        channelId: req.body.channelId
    })
    return res.end(JSON.stringify(result))
})

post('/save-channel-bot-mode', async (req, res) => {
    let result = await req.userSession.saveChannelBotMode(req.body.channelId, req.body.botId, req.body.mode)
    return res.end(JSON.stringify(result))
})

post('/save-forum-bot-mode', async (req, res) => {
    let result = await req.userSession.saveForumBotMode(req.body.forumId, req.body.botId, req.body.mode)
    return res.end(JSON.stringify(result))
})

post('/save-learning', async (req, res) => {
    let result = await req.userSession.saveLearning({
        botId: req.body.botId,
        correctionMode: req.body.correctionMode,
        language: req.body.language,
        LearningMode: req.body.learningMode,
        learningRate: req.body.learningRate
    })
    return res.end(JSON.stringify(result))
})

post('/save-voice', async (req, res) => {
    let result = await req.userSession.saveVoice(req.body.botId, req.body.voice, req.body.mod, req.body.language, req.body.pitch, req.body.nativeVoice)
    return res.end(JSON.stringify(result))
})

post('/save-bot-avatar', async(req, res)=>{
    let result = await req.userSession.saveBotAvatar(req.body.botId, req.body.avatarId)
    return res.end(JSON.stringify(result))
})

post('/user-admin', async(req, res)=>{
    let result = await req.userSession.userAdmin(req.body.botId, req.body.type, req.body.operation, req.body.operationUser)
    return res.end(JSON.stringify(result))
})

post('/create-avatar', async(req, res)=>{
    let result = await req.userSession.createAvatar({
        name: req.body.name,
        accessMode: req.body.accessMode,
        categorise: req.body.categories,
        description: req.body.description,
        details: req.body.details,
        disclamier: req.body.disclaimer,
        isHidden: req.body.isHidden,
        isPrivate: req.body.isPrivate,
        license: req.body.license
    })
    return res.end(JSON.stringify(result))
})


mainRouter.post('/create-graphic-media', sdkAuth, upload.single('file'), async (req, res) => {
    const uploadedFile = req.file;
    if (!uploadedFile) {
        return res.status(400).json({ error: 'No file uploaded' });
    }
    let result = await req.userSession.createGraphicMedia(
        req.body.graphicId,
        new Blob([uploadedFile.buffer], { type: uploadedFile.mimetype }),
        uploadedFile.originalname,
        uploadedFile.mimetype
    )
    return res.end(JSON.stringify(result))
})

mainRouter.post('/update-user-icon', sdkAuth, upload.single('file'), async (req, res) => {
    const uploadedFile = req.file;
    if (!uploadedFile) {
        return res.status(400).json({ error: 'No file uploaded' });
    }
    let result = await req.userSession.updateUserIcon(
        new Blob([uploadedFile.buffer], { type: uploadedFile.mimetype }),
        uploadedFile.originalname
    )
    return res.end(JSON.stringify(result))
})


post('/get-forum-bot-mode', async(req, res)=>{
    let result = await req.userSession.getForumBotMode(req.body.forumId)
    return res.end(JSON.stringify(result))
})

post('/get-voice', async(req, res)=>{
    let result = await req.userSession.getVoice(req.body.instanceId, req.body.id)
    return res.end(JSON.stringify(result))
})

post('/get-default-response', async(req, res)=>{
    let result = await req.userSession.getDefaultResponse(req.body.instanceId, req.body.id)
    return res.end(JSON.stringify(result))
})

post('/get-greetings', async(req, res)=>{
    let result = await req.userSession.getGreetings(req.body.instanceId, req.body.id)
    return res.end(JSON.stringify(result))
})


post('/get-responses', async(req, res)=>{
    let result = await req.userSession.getResponses({
        instance: req.body.instance,
        duration: req.body.duration,
        filter: req.body.filter,
        inputType: req.body.inputType,
        responseType: req.body.responseType,
        restrict: req.body.restrict
    })
    return res.end(JSON.stringify(result))
})

post('/get-conversation', async(req, res)=>{
    let result = await req.userSession.getConversations(req.body.instanceId, req.body.responseId, req.body.duration, req.body.sort, req.body.inputType)
    return res.end(JSON.stringify(result))
})

post('/browse', async(req, res)=>{
    let result = await req.userSession.browse(req.body.type, req.body.typeFilter, req.body.contentRating)
    return res.end(JSON.stringify(result))
})


post('/get-avatar-media', async(req, res)=>{
    let result = await req.userSession.getAvatarMedia(req.body.avatarId)
    return res.end(JSON.stringify(result))
})


post('/get-script-source', async(req, res)=>{
    let result = await req.userSession.getScriptSource(req.body.scriptId)
    return res.end(JSON.stringify(result))
})


post('/save-script-source', async(req, res)=>{
    let result = await req.userSession.saveScriptSource(req.body.scriptId)
    return res.end(JSON.stringify(result))
})




mainRouter.post('/save-avatar-background', sdkAuth, upload.single('file'), async (req, res) => {
    const uploadedFile = req.file;
    if (!uploadedFile) {
        return res.status(400).json({ error: 'No file uploaded' });
    }
    let result = await req.userSession.saveAvatarBackground({
        avatarId: req.body.avatarId,
        image: new Blob([uploadedFile.buffer], { type: uploadedFile.mimetype }),
        name: uploadedFile.originalname,
        fileType: uploadedFile.mimetype
    })
    return res.end(JSON.stringify(result))
})

post('delete-avatar-background', async (req, res) => {
    let result = await req.userSession.deleteAvatarBackground(req.body.avatarId)
    return res.end(JSON.stringify(result))
})


post('/update-user', async (req, res) => {
    let result = await req.userSession.updateUser({
        user: req.body.user,
        bio: req.body.bio,
        email: req.body.email,
        hint: req.body.hint,
        name: req.body.name,
        password: req.body.password,
        showName: req.body.showName,
        website: req.body.website
    })
    return res.end(JSON.stringify(result))
})

post('/avatar-message', async (req, res) => {
    let result = await req.userSession.avatarMessage({
        applicationId: req.body.applicationId,
        instance: req.body.instance,
        avatar: req.body.avatar,
        speak: req.body.speak,
        message: req.body.message,
        emote: req.body.emote,
        action: req.body.action,
        pose: req.body.pose,
        voice: req.body.voice,
        format: req.body.format,
        hd: req.body.hd
    })
    return res.end(JSON.stringify(result))
})

post('/create-forum-post', async (req, res) => {
    let result = await req.userSession.createForumPost(
        req.body.forumId,
        req.body.topic,
        req.body.detials,
        req.body.tags
    )
    return res.end(JSON.stringify(result))
})

post('/update-forum-post', async (req, res) => {
    let result = await req.userSession.updateForumPost(
        req.body.forumId,
        req.body.postId,
        req.body.topic,
        req.body.detials,
        req.body.tags
    )
    return res.end(JSON.stringify(result))
})

post('delete-forum-post', async (req, res) => {
    let result = await req.userSession.deleteForumPost(req.body.forumId, req.body.postId)
    return res.end(JSON.stringify(result))
})

mainRouter.post('/create-avatar-media', sdkAuth, upload.single('file'), async (req, res) => {
    const uploadedFile = req.file;
    if (!uploadedFile) {
        return res.status(400).json({ error: 'No file uploaded' });
    }
    let result = await req.userSession.createAvatarMedia(
        req.body.avatarId,
        new Blob([uploadedFile.buffer], { type: uploadedFile.mimetype }),
        uploadedFile.originalname,
        uploadedFile.mimetype
    )
    return res.end(JSON.stringify(result))
})

post('/save-avatar-media', async (req, res) => {
    let result = await req.userSession.saveAvatarMedia({
        avatarId: req.body.avatarId,
        mediaId: req.body.mediaId,
        actions: req.body.actions,
        emotions: req.body.emotions,
        hd: req.body.hd,
        poses: req.body.poses,
        talking: req.body.talking
    })
    return res.end(JSON.stringify(result))
})

post('/delete-avatar-media', async (req, res) => {
    let result = await req.userSession.deleteAvatarMedia(req.body.avatarId, req.body.mediaId)
    return res.end(JSON.stringify(result))
})

post('/flag-user', async (req, res) => {
    let result = await req.userSession.flagUser(req.body.userId, req.body.flaggedReason)
    return res.end(JSON.stringify(result))
})


post('/subscribe-post', async (req, res) => {
    let result = await req.userSession.subscribeForumPost(req.body.postId)
    return res.end(JSON.stringify(result))
})


post('/unsubscribe-post', async (req, res) => {
    let result = await req.userSession.unsubscribeForumPost(req.body.postId)
    return res.end(JSON.stringify(result))
})

post('/subscribe-forum', async (req, res) => {
    let result = await req.userSession.subscribeForum(req.body.forumId)
    return res.end(JSON.stringify(result))
})

post('/unsubscribe-forum', async (req, res) => {
    let result = await req.userSession.unsubscribeForum(req.body.forumId)
    return res.end(JSON.stringify(result))
})

post('/get-forum-posts', async (req, res) => {
    let result = await req.userSession.getForumPosts({
        sort: req.body.sort,
        type: req.body.type,
        typeFilter: req.body.typeFilter
    })
    return res.end(JSON.stringify(result))
})

post('/thumbs-up-forum', async (req, res) => {
    let result = await req.userSession.thumbsUp(new ForumConfig(), req.body.forumId)
    return res.end(JSON.stringify(result))
})

post('/thumbs-down-forum', async (req, res) => {
    let result = await req.userSession.thumbsUp(new ForumConfig(), req.body.forumId)
    return res.end(JSON.stringify(result))
})


post('/thumbs-up-post', async (req, res) => {
    let result = await req.userSession.thumbsUpPost(new ForumPostConfig(), req.body.postId)
    return res.end(JSON.stringify(result))
})

post('/thumbs-down-post', async (req, res) => {
    let result = await req.userSession.thumbsDownPost(new ForumPostConfig(), req.body.postId)
    return res.end(JSON.stringify(result))
})

post('/star-forum', async (req, res) => {
    let result = await req.userSession.starForum(req.body.forumId, req.body.star)
    return res.end(JSON.stringify(result))
})

post('/tts', async (req, res) => {
    let result = await req.userSession.TTS(req.body.voice, req.body.text, req.body.mod)
    return res.end(JSON.stringify(result))
})


post('/get-forum-admins', async (req, res) => {
    let result = await req.userSession.getAdminsForum({ forumId: req.body.forumId })
    return res.end(JSON.stringify(result))
})

post('/get-forum-users', async (req, res) => {
    let result = await req.userSession.getUsersForum({ forumId: req.body.forumId })
    return res.end(JSON.stringify(result))
})

post('get-categories', async (req, res) => {
    let result = await req.userSession.getCategories({ type: req.body.type })
    return res.end(JSON.stringify(result))
})

post('/get-tags', async (req, res) => {
    let result = await req.userSession.getTags({ type: req.body.type })
    return res.end(JSON.stringify(result))
})

post('/get-all-templates', async (req, res) => {
    let result = await req.userSession.getAllTemplates()
    return res.end(JSON.stringify(result))
})



mainRouter.post('/create-channel-attachment', sdkAuth, upload.single('file'), async (req, res) => {
    const uploadedFile = req.file;
    if (!uploadedFile) {
        return res.status(400).json({ error: 'No file uploaded' });
    }
    let result = await req.userSession.createChannelFileAttachment(
        req.body.instanceId,
        new Blob([uploadedFile.buffer], { type: uploadedFile.mimetype }),
        uploadedFile.originalname,
        uploadedFile.mimetype
    )
    return res.end(JSON.stringify(result))
})

post('/create-reply', async (req, res) => {
    let result = await req.userSession.createReply(req.body.forumId, req.body.postId, req.body.details, req.body.tags)
    res.end(JSON.stringify(result))
})

post('/create-user-message', async (req, res) => {
    let result = await req.userSession.createUserMessage(req.body.toUser, req.body.message)
    return res.end(JSON.stringify(result))
})

post('/save-response', async (req, res) => {
    let result = await req.userSession.saveResponse({
        botId: req.body.botId,
        actions: req.body.actions,
        command: req.body.command,
        correctness: req.body.correctness,
        emotions: req.body.emotions,
        keywords: req.body.keywords,
        label: req.body.label,
        noRepeat: req.body.noRepeat,
        onRepeat: req.body.onRepeat,
        poses: req.body.poses,
        previous: req.body.previous,
        question: req.body.question,
        questionId: req.body.questionId,
        required: req.body.required,
        requirePrevious: req.body.requirePrevious,
        requireTopic: req.body.requireTopic,
        response: req.body.response,
        responseId: req.body.responseId,
        topic: req.body.topic,
        type: req.body.type
    })
    return res.end(JSON.stringify(result))
})

post('/delete-response', async (req, res) => {
    let result = await req.userSession.deleteResponse(req.body.responseId, req.body.questionId, req.body.type)
    return res.end(JSON.stringify(result))
})

