import os
from fastapi import FastAPI, HTTPException, Query
from telethon import TelegramClient
from telethon.tl.functions.messages import (GetMessagesViewsRequest, GetDiscussionMessageRequest,
                                            GetMessagesReactionsRequest, ImportChatInviteRequest)

app = FastAPI()

POSITIVE_REACTIONS = ['⚡', '🔥', '🤯', '👍', '❤', '🥰', '👏', '😁', '🎉', '🤩', '🙏', '👌', '🕊', '😍', '❤‍🔥', '💯',
                      '🤣', '🏆', '🍌', '🍓', '🍾', '💋', '😇', '🤝', '🤗', '🫡', '🎄', '☃', '💅', '💘', '😘', '💊',
                      '😎', '🤪', '🗿', '🆒', '🙉', '🦄', '👾', '😂']

NEGATIVE_REACTIONS = ['👎', '🤬', '🤮', '💩', '💔', '😨', '😴', '😈', '🖕', '😡', '🤡']

NEUTRAL_REACTIONS = ['🤔', '😱', '🤨', '😐', '🌚', '🌭', '🤨', '👀', '🙈', '🤓', '👻', '👨‍💻', '🎃', '🤷‍♂', '🤷',
                     '🤷‍♀', '🥱', '🥴', '🐳', '🤨']

api_id = int(os.environ.get("TELEGRAM_VIEWS_SERVICE_API_ID"))
api_hash = os.environ.get("TELEGRAM_VIEWS_SERVICE_API_HASH")
phone = os.environ.get("TELEGRAM_VIEWS_SERVICE_PHONE")

client = TelegramClient('sessions/test_bot', api_id, api_hash)

@app.on_event("startup")
async def startup_event():
    await client.start(phone=lambda: phone)


@app.get("/views")
async def get_views(peerId: int = Query(...), messageId: int = Query(...)):
    try:
        response = await client(GetMessagesViewsRequest(
            peer=peerId,
            id=[messageId],
            increment=False
        ))

        return {
            "count": response.views[0].views
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/comments")
async def get_comments(peerId: int = Query(...), messageId: int = Query(...)):
    try:
        response = await client(GetDiscussionMessageRequest(
            peer=peerId,
            msg_id=messageId
        ))

        return {
            "count": response.unread_count
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/reactions")
async def get_reactions(peerId: int = Query(...), messageId: int = Query(...)):
    try:
        response = await client(GetMessagesReactionsRequest(
            peer=peerId,
            id=[messageId]
        ))

        if not response.updates:
            return {
                "positive_count": 0,
                "negative_count": 0,
                "neutral_count": 0
            }

        response = response.updates[0]
        reactions = response.reactions.results

        negative_reactions = 0
        positive_reactions = 0
        neutral_reactions = 0

        for type in reactions:
            emoji = type.reaction.emoticon
            count = type.count

            if emoji in POSITIVE_REACTIONS:
                positive_reactions += count
            elif emoji in NEGATIVE_REACTIONS:
                negative_reactions += count
            elif emoji in NEUTRAL_REACTIONS:
                neutral_reactions += count

        return {
            "positive_count": positive_reactions,
            "negative_count": negative_reactions,
            "neutral_count": neutral_reactions
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/join")
async def join_channel(hash: str = Query(...)):
    try:
        await client(ImportChatInviteRequest(hash=hash))
        return {
            "status": "ok"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=int(os.environ.get("TELEGRAM_VIEWS_SERVICE_PORT", 8081)))
