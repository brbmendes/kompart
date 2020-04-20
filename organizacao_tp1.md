**Sistema P2P basico**

- Arquitetura centralizada (servidor)

- Um único programa, com 2 modos de operação (servidor e cliente)

**Inicializar**
- kompart <ip> <type>"s(server) || c(client)"
*OBS: Não foi realizado nenhum tipo de controle que identifique se já existe um server ativo ou não. Supõe-se que exista somente 1 server ativo.*

**Comandos:**
- registry client nick
- registry path-to-file/filename
- list files
- get files[] | onde file é IP/filename ou IP/hash
- get all IP

**Funcionamento dos comandos**
*registry client nick*
	- Erros: nick ja cadastrado
	Servidor busca a lista de peers pelo nick. Se não tiver nenhum peer com o nick solicitado, ele salva as informações do client no server. Se tiver, retorna a mensagem "Nick ja cadastrado"
	
*registry path-to-file/filename*
	- Erros: client não registrado
	- Funcionamento: O servidor deve buscar o client pelo IP. ao localizar, associa os arquivos enviados ao client. Se não localizar, retorna a mensagem "Client não registrado"
	
	Ao realizar o comando, o client calcula o hash do arquivo e envia o nome completo (com caminho completo) e o hash do arquivo ao servidor.
	
*list files*
	- Funcionamento: servidor informa a lista de arquivos (nick / nome / hash) em cada peer. Se não tiver arquivo registrado, exibe a mensagem "Sem arquivos registrados"

*get files[]*
	- Erros: [1] Client busca arquivo de outro client que já não está mais ativo. Retorna mensagem "Client (nick) offline".
			[2] Client busca arquivo já removido. Retorna mensagem "arquivo removido ou indisponível".
	- Funcionamento: Client A solicita um arquivo ao client B. Se o client B não estiver online, servidor retorna mensagem [1]. Se o arquivo não existir mais no client B, client B envia a mensagem [B]. Se existir, o client B envia os arquivos ao Client A.
	
*get all IP*
	- Erros: 	[1] Client busca arquivo de outro client que já não está mais ativo. Retorna mensagem "Client (nick) offline".
			[2] Client busca arquivo já removido. Retorna mensagem "arquivo removido ou indisponível".
	- Funcionamento: Client A solicita um arquivo ao client B. Se o client B não estiver online, servidor retorna mensagem [1]. Se o arquivo não existir mais no client B, client B envia a mensagem [B]. Se existir, o client B envia todos os arquivos ao Client A.


**Hearbeat**
- Para manter a rede de *overlay*, utilizar o mecanismo de heartbeat semelhante a atividade 7.

**Recebimento assincrono**
Utilizar uma thread separada que recebe as mensagens do servidor, e exibe na tela ao usuário.

**Estruturas para armazenamento**
*para servidor armazenar clientes*
	hashmap<nick,peer>
	
	class peer
		String nick;
        InetAddress addr;
		int port;
		int heartbeat;
		bool active
		hashmap<filename,file>
	
	class file
		String filename;
        string filedir;
		string hash 
		int size_in_bytes

*para servidor armazenar a lista de recursos a serem enviados*
arrayList<hashmap<peer destinatario,file arquivo>>

