**Sistema P2P basico**

- Arquitetura centralizada (servidor)
- Um único programa, com 2 modos de operação (servidor e peer)

**Inicializar**
Pode rodar como Servidor <s> ou como peer <p>
- kompart <server ip> s
- kompart <server ip> p <host ip>
*OBS: Não foi realizado nenhum tipo de controle que identifique se já existe um server ativo ou não. Supõe-se que exista somente 1 server ativo.*

**Comandos:**

- register peer
- register file <path/filename>
- list files
- get file IP/filename ou IP/hash
- disconnect

**Funcionamento dos comandos**
*register peer*
	- Erros: 	[1] IP já registrado. Retorna mensagem: "Peer <IP> já registrado"
	Servidor busca a lista de peers pelo IP. Se não tiver nenhum peer com o IP solicitado, ele salva as informações do peer no server. Se tiver, retorna a mensagem [1].
	
*register file filename*
	- Erros: 	[1] Peer não registrado. Retorna mensagem: "Peer <IP> não registrado"
				[2] Arquivo já registrado Retorna mensagem: "Arquivo nomeArquivo já registrado"
	- Funcionamento: O servidor deve buscar o peer pelo IP. ao localizar, associa os arquivos enviados ao peer. Se não localizar peer, retorna a mensagem [1]. Se o arquivo já foi registrado, retorna a mensagem [2]. OBS: O peer digita "register file filename", mas na verdade ele manda ao servidor "register file filename hash"
	
	Ao realizar o comando, o peer calcula o hash do arquivo e envia o nome completo (com caminho completo) e o hash do arquivo ao servidor.
	
*list files*
	- Erros: 	[1] Sem arquivos registrados. Retorna mensagem "Sem arquivos registrados".
				[2] Peer não registrado. Retorna mensagem "Peer <IP> não registrado.
	- Funcionamento: servidor informa a lista de arquivos (IP / nome / hash) em cada peer. Se não tiver arquivo registrado, exibe a mensagem [1]. Caso o peer que esteja solicitando a lista de arquivos não esteja registrado, retorna a mensagem [2].

*get file IP/filename ou IP/hash*
	- Erros: 	[1] Peer busca arquivo de outro peer que já não está mais ativo. Retorna mensagem "Peer <IP> offline".
				[2] Peer busca arquivo já removido. Retorna mensagem "arquivo removido ou indisponível".
	- Funcionamento: peer A solicita um arquivo ao peer B. Se o peer B não estiver online, servidor retorna mensagem [1]. Se o arquivo não existir mais no peer B, peer B envia a mensagem [B]. Se existir, o peer B envia os arquivos ao peer A.
	
*disconnect*
	- Erros:	[1] Peer não conectado. Retorna a mensagem "Peer não registrado".
	- Funcionamento: Peer se desconecta do servidor. Se não estiver conectado, exibe a mensagem "Peer não registrado"


**Hearbeat**
- Foi criada uma thread separada para processar o recebimento do heartbeat.

**Recebimento assincrono**
- Foi criada uma thread separada para processar o recebimento das mensagens.