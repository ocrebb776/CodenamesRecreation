        let String_name
        let String_Code
        let String_team
        let isAnimating = false
        function submitForm() {
            let form = document.forms.joinForm
            String_name = form.name.value
            String_Code = form.enterCode.value
            String_team = form.selectTeam.value

            sendCustomRequest("join", String_name, String_team, String_Code, "", loginRequest, handleError);
        }

        let loginInfo = {
            name: "",
            code: "",
            id: "",
            set(name, code, id) {
                console.log("set")
                this.name = name
                this.code = code
                this.id = id
            }
        }

        function updateGamePage() {
            if(isAnimating || spymasterOn){
                setTimeout(updateGamePage,1000)
            }else{
            sendCustomRequest("update", loginInfo.name, "", loginInfo.code, loginInfo.id, updatePageFromRequest, handleError)
            }
        }


        function updatePageFromRequest(data) {
            let gameView = document.getElementById("gameView")
            gameView.innerHTML = "";
            if (data.success && data.responseType === "update") {
                x = 0
                y = 0
                cards = data.cards
                document.getElementById("currTeam").innerHTML = data.currentTurn
                data.cards.forEach(row => {
                    y=0
                    row.forEach(e => {
                        piece = document.createElement("div")
                        piece.classList.add("cardDisp")
                        piece.setAttribute("cardType",e.role)
                        if(e.flipped){
                            piece.classList.add(e.role)
                        }
                        piece.innerHTML = e.text;
                        piece.textContent = e.text
                        piece.setAttribute("id",`CARD(${x},${y})`)
                        let xClick = x
                        let yClick = y
                        piece.addEventListener("click",(event)=>{
                            handleClick(xClick,yClick)
                        })


                        gameView.appendChild(piece)
                        y++;
                    })
                    x++;
                })


            } else {
              //  alert(data.responseText)
            }

            console.log(data)
        }

function handleClick(x,y) {
            sendCustomRequest("Click", loginInfo.name, x+","+y, loginInfo.code, loginInfo.id, ()=>{}, handleError)
    
    
}
        function handleError(error) {
            alert("There was an error accessing the server" + error)
        }

        function loginRequest(data) {
            if (data.success && data.responseType === "SuccessfulJoinId") {
                loginInfo.set(String_name, String_Code, data.responseContent)
                updateGamePage()
                 setInterval(updateGamePage,10000)
                document.getElementById("formCont").outerHTML = "";
                document.getElementById("gameCard").style.display = ""
 startListening()
 document.getElementById("code").innerHTML = "Join Code: "+String_Code

            } else {
                alert(data.responseContent)
            }
        }

        /**
         * Sends a POST request with the provided parameters as the JSON body.
         *  @param {string} requestType - The type of request.
         * @param {string} requestName - The name of the request.
         * @param {object} requestBody - The main content/body of the request.
         * @param {string|number} requestCode - A code associated with the request.
         * @param {string|number} requestID - The unique ID of the request.
         * @param {function} successFunction - Callback to run on success.
         * @param {function} errorFunction - Callback to run on failure.
         */
        function sendCustomRequest(
            requestType,
            requestName,
            requestBody,
            requestCode,
            requestID,
            successFunction,
            errorFunction
        ) {
            // 1. Construct the payload object using ES6 property shorthand
            // This bundles the non-function arguments into a single object
            const payload = {
                requestType,
                requestName,
                requestBody,
                requestCode,
                requestID
            };

            // 2. Define the target URL (You can hard-code this or pass it as an argument)
            const targetUrl = '/request/';

            // 3. Send the POST request
            fetch(targetUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload) // Convert object to JSON string
            })
                .then(response => {
                    // Check if the network response is valid (status 200-299)
                    if (!response.ok) {
                        throw new Error(`HTTP Error! Status: ${response.status}`);
                    }
                    // Parse the JSON response
                    return response.json();
                })
                .then(data => {
                    // 4. Execute the success callback
                    if (successFunction && typeof successFunction === 'function') {
                        successFunction(data);
                    }
                })
                .catch(error => {
                    // 5. Execute the error callback
                    if (errorFunction && typeof errorFunction === 'function') {
                       // errorFunction(error);
                    }
                });
        }

        function startListening(){
            const socket = new WebSocket(`http://${window.location.hostname}:8080`); // Use your actual port

// Connection opened
socket.addEventListener('open', function (event) {
    console.log('Connected to WebSocket server');
    socket.send(JSON.stringify(
        {
            requestName:loginInfo.name,
            requestCode:loginInfo.code,
            requestID:loginInfo.id
        }
    ));
});

// Listen for messages
socket.addEventListener('message', function (event) {
    console.log('Message from server:', event.data);
    data = JSON.parse(event.data);
    console.log(data)
    if(data.type === "CARDFLIP"){
         card = document.getElementById("CARD("+data.coords+")")
        let coords = data.coords.split(",")
        cards[Number(coords[0])][Number(coords[1])].flipped = true
        flipCard(card,()=>{
             card.classList.add(card.getAttribute("cardType"))
            card.classList.add("flippedCard")
        })
    }else if(data.type === "fullUpdate"){
        updateGamePage()
    }else if(data.type ==="newTeam"){
        document.getElementById("currTeam").innerHTML = data.team
    }else if(data.type === "ENDGAME"){
        alert(`  Game Over! \n${data.winTeam} Won! \nReason:${data.reason} `)
    }
});

// Connection closed
socket.addEventListener('close', function (event) {
    console.log('WebSocket connection closed');
});
        }
        function flipCard(card,midAnimate){
           
            
            let intId = null
            let stage = -20;
            let rotate = 0
            let scale = 1
            clearInterval(intId)
             function frame(){
                try{
                if(stage >= 123){
                    isAnimating = false
                    clearInterval(intId)
                }
                    else if(stage > 100){
                    card.style.scale = scale
                    scale-=0.02
                        
                }else if(stage >50){
                    rotate++
                    
                    card.style.transform = `rotateY(${-90+rotate*90/50}deg)`
                
                }else if(stage === 50){
                    rotate = 0
                    
                   midAnimate()
                  
                }
                     else if(stage > 0){
                    rotate++
               
                    card.style.transform = `rotateY(${rotate*90/50}deg)`
                  
                }else{
        
                    card.style.scale = scale
                    scale+=0.02
                   
                }
                stage++
            }catch (error) {
                        console.error(error)
                        clearInterval(intId)
                    }
            }
            isAnimating = true;
            intId = setInterval(frame,5);
           
        }



function finishTurn(){
    sendCustomRequest("NextTurn",loginInfo.name,"",loginInfo.code,loginInfo.id,(data)=>{
        console.log(data);
    })
}


        spymasterOn = false

        function toggleSpymaster() {
            for (x = 0; x < cards.length; x++) {
                for (y = 0; y < cards[x].length; y++) {
                    let card = document.getElementById(`CARD(${x},${y})`)
                    console.log(card)
                    if (!cards[x][y].flipped) {
                        if (spymasterOn) {
                            flipCard(card, () => {
                                card.classList.remove(card.getAttribute("cardType"))
                            })
                        } else {
                            flipCard(card, () => {

                                card.classList.add(card.getAttribute("cardType"))
                            })
                        }
                    }else{
                        if (spymasterOn) {
                            flipCard(card, () => {
                                card.classList.remove("flippedCard")
                            })
                        } else {
                            flipCard(card, () => {

                                card.classList.add("flippedCard")
                            })
                        }
                    }

                }
            }
            spymasterOn = !spymasterOn

        }


