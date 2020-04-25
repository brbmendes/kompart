**Sistema P2P basico**

- Arquitetura centralizada (servidor)
- Um único programa, com 2 modos de operação (servidor e cliente)

**Inicializar**
- kompart <ip> <type>"s(server) || p(peer)"
*OBS: Não foi realizado nenhum tipo de controle que identifique se já existe um server ativo ou não. Supõe-se que exista somente 1 server ativo.*

**Comandos:**

- register peer
- register file <path/filename>
- list files
- get files[] | onde file é IP/filename ou IP/hash
- get all IP
- disconnect

**Funcionamento dos comandos**
*register peer*
	- Erros: IP já registrado. Mensagem: "Peer <IP> já registrado"
	Servidor busca a lista de peers pelo IP. Se não tiver nenhum peer com o IP solicitado, ele salva as informações do peer no server. Se tiver, retorna a mensagem [1].
	
*register file filename*
	- Erros: [1] Peer não registrado. Mensagem: "Peer <IP> não registrado"
			[2] Arquivo já registrado Mensagem: "Arquivo nomeArquivo já registrado"
	- Funcionamento: O servidor deve buscar o peer pelo IP. ao localizar, associa os arquivos enviados ao peer. Se não localizar peer, retorna a mensagem [1]. Se o arquivo já foi registrado, retorna a mensagem [2]. OBS: O peer digita "register file filename", mas na verdade ele manda ao servidor "register file filename hash"
	
	Ao realizar o comando, o peer calcula o hash do arquivo e envia o nome completo (com caminho completo) e o hash do arquivo ao servidor.
	
*list files*
	- Funcionamento: servidor informa a lista de arquivos (IP / nome / hash) em cada peer. Se não tiver arquivo registrado, exibe a mensagem "Sem arquivos registrados"

*get files[]*
	- Erros: [1] Peer busca arquivo de outro peer que já não está mais ativo. Retorna mensagem "Peer <IP> offline".
			[2] Peer busca arquivo já removido. Retorna mensagem "arquivo removido ou indisponível".
	- Funcionamento: peer A solicita um arquivo ao peer B. Se o peer B não estiver online, servidor retorna mensagem [1]. Se o arquivo não existir mais no peer B, peer B envia a mensagem [B]. Se existir, o peer B envia os arquivos ao peer A.
	
*get all IP*
	- Erros: 	[1] Peer busca arquivo de outro peer que já não está mais ativo. Retorna mensagem "Peer <IP> offline".
			[2] Peer busca arquivo já removido. Retorna mensagem "arquivo removido ou indisponível".
	- Funcionamento: peer A solicita um arquivo ao peer B. Se o peer B não estiver online, servidor retorna mensagem [1]. Se o arquivo não existir mais no peer B, peer B envia a mensagem [B]. Se existir, o peer B envia todos os arquivos ao peer A.

*disconnect*
	- Erros:	[1] Peer não conectado. Retorna a mensagem "Peer não registrado".
	- Funcionamento: Peer se desconecta do servidor. Se não estiver conectado, exibe a mensagem "Peer não registrado"


**Hearbeat**
- Para manter a rede de *overlay*, utilizar o mecanismo de heartbeat semelhante a atividade 7.

**Recebimento assincrono**
Utilizar uma thread separada que recebe as mensagens do servidor, e exibe na tela ao usuário.

**Estruturas para armazenamento**
*para servidor armazenar peers*
	hashmap<IP,peer>
	
	class peer
        InetAddress addr;
		int port;
		int heartbeat;
		bool active
		hashmap<filename,kfile>
	
	class kfile
		String filename;
		string hash 

*para peer armazenar a lista de recursos a serem enviados*
arrayList<hashmap<peer destinatario,file arquivo>>

