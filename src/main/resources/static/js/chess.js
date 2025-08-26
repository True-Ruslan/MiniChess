/**
 * Modern Chess Game Frontend
 * Uses ES6+ features, modern APIs, and clean architecture
 */

// Chess piece icons mapping - using local SVG files from cburnett set
const PIECE_ICONS = {
    'WHITE_KING': '/images/piece/cburnett/wK.svg',
    'WHITE_QUEEN': '/images/piece/cburnett/wQ.svg',
    'WHITE_ROOK': '/images/piece/cburnett/wR.svg',
    'WHITE_BISHOP': '/images/piece/cburnett/wB.svg',
    'WHITE_KNIGHT': '/images/piece/cburnett/wN.svg',
    'WHITE_PAWN': '/images/piece/cburnett/wP.svg',
    'BLACK_KING': '/images/piece/cburnett/bK.svg',
    'BLACK_QUEEN': '/images/piece/cburnett/bQ.svg',
    'BLACK_ROOK': '/images/piece/cburnett/bR.svg',
    'BLACK_BISHOP': '/images/piece/cburnett/bB.svg',
    'BLACK_KNIGHT': '/images/piece/cburnett/bN.svg',
    'BLACK_PAWN': '/images/piece/cburnett/bP.svg'
};

// Game state management
class GameState {
    constructor() {
        this.selectedSquare = null;
        this.legalMoves = [];
        this.sideToMove = 'WHITE';
        this.inCheck = false;
        this.whiteInCheck = false;
        this.blackInCheck = false;
        this.isBoardFlipped = false;
        this.moves = [];
    }

    reset() {
        this.selectedSquare = null;
        this.legalMoves = [];
        this.sideToMove = 'WHITE';
        this.inCheck = false;
        this.whiteInCheck = false;
        this.blackInCheck = false;
        this.moves = [];
    }

    updateFromBoardData(boardData) {
        this.sideToMove = boardData.sideToMove;
        this.inCheck = boardData.inCheck || false;
        this.whiteInCheck = boardData.whiteInCheck || false;
        this.blackInCheck = boardData.blackInCheck || false;
    }
}

// API service for backend communication
class ChessAPI {
    static async getBoard() {
        try {
            const response = await fetch('/api/board');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error fetching board:', error);
            throw error;
        }
    }

    static async getLegalMoves(from) {
        try {
            const response = await fetch(`/api/moves?from=${from}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            return data.moves || [];
        } catch (error) {
            console.error('Error fetching legal moves:', error);
            return [];
        }
    }

    static async makeMove(from, to) {
        try {
            const response = await fetch('/api/move', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({from, to})
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Move failed');
            }

            return await response.json();
        } catch (error) {
            console.error('Error making move:', error);
            throw error;
        }
    }

    static async getMoveList() {
        try {
            const response = await fetch('/api/move-list');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Error fetching move list:', error);
            return [];
        }
    }

    static async resetGame() {
        try {
            const response = await fetch('/api/reset', {method: 'POST'});
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return true;
        } catch (error) {
            console.error('Error resetting game:', error);
            throw error;
        }
    }
}

// Board rendering and interaction
class ChessBoard {
    constructor() {
        this.gameState = new GameState();
        this.boardElement = document.getElementById('chess-board');
        this.boardWrapper = document.getElementById('board-wrapper');
        this.currentPlayerElement = document.getElementById('current-player');
        this.movesListElement = document.getElementById('moves-list');

        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Square click handlers
        this.boardElement.addEventListener('click', (e) => {
            const square = e.target.closest('.square');
            if (square) {
                this.handleSquareClick(square);
            }
        });

        // New game button
        document.getElementById('new-game-btn').addEventListener('click', () => {
            this.showNewGameModal();
        });

        // Flip board button
        document.getElementById('flip-board-btn').addEventListener('click', () => {
            this.flipBoard();
        });

        // Modal handlers
        document.getElementById('cancel-btn').addEventListener('click', () => {
            this.hideNewGameModal();
        });

        document.getElementById('confirm-btn').addEventListener('click', () => {
            this.startNewGame();
        });

        // Close modal on backdrop click
        document.getElementById('confirm-modal').addEventListener('click', (e) => {
            if (e.target.id === 'confirm-modal') {
                this.hideNewGameModal();
            }
        });

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideNewGameModal();
                this.clearSelection();
            }
        });
    }

    async handleSquareClick(squareElement) {
        const squareName = squareElement.dataset.square;

        // If clicking the same square, deselect
        if (this.gameState.selectedSquare === squareName) {
            this.clearSelection();
            return;
        }

        // If clicking a legal move, make the move
        if (this.gameState.selectedSquare && this.gameState.legalMoves.includes(squareName)) {
            await this.makeMove(this.gameState.selectedSquare, squareName);
            return;
        }

        // If clicking a piece, select it and show legal moves
        const piece = squareElement.querySelector('img');
        if (piece) {
            const pieceColor = piece.dataset.pieceColor;
            if (pieceColor === this.gameState.sideToMove) {
                await this.selectPiece(squareName);
            }
        } else {
            this.clearSelection();
        }
    }

    async selectPiece(squareName) {
        try {
            const legalMoves = await ChessAPI.getLegalMoves(squareName);
            this.gameState.selectedSquare = squareName;
            this.gameState.legalMoves = legalMoves;
            this.highlightLegalMoves(squareName, legalMoves);
        } catch (error) {
            console.error('Error selecting piece:', error);
            this.showError('Ошибка при получении доступных ходов');
        }
    }

    async makeMove(from, to) {
        try {
            this.setLoading(true);
            const boardData = await ChessAPI.makeMove(from, to);
            this.gameState.updateFromBoardData(boardData);
            await this.renderBoard(boardData);
            await this.updateMovesList();
            this.updateCurrentPlayer();
            this.clearSelection();
        } catch (error) {
            console.error('Error making move:', error);
            this.showError(error.message || 'Ошибка при выполнении хода');
        } finally {
            this.setLoading(false);
        }
    }

    highlightLegalMoves(selectedSquare, moves) {
        // Clear previous highlights
        this.clearHighlights();

        // Highlight selected square
        const selectedElement = document.querySelector(`[data-square="${selectedSquare}"]`);
        if (selectedElement) {
            selectedElement.classList.add('selected');
        }

        // Highlight legal moves
        moves.forEach(move => {
            const moveElement = document.querySelector(`[data-square="${move}"]`);
            if (moveElement) {
                const hasPiece = moveElement.querySelector('img');
                if (hasPiece) {
                    moveElement.classList.add('legal-capture');
                } else {
                    moveElement.classList.add('legal-move');
                }
            }
        });
    }

    clearSelection() {
        this.gameState.selectedSquare = null;
        this.gameState.legalMoves = [];
        this.clearHighlights();
    }

    clearHighlights() {
        document.querySelectorAll('.square').forEach(square => {
            square.classList.remove('selected', 'legal-move', 'legal-capture', 'in-check');
        });
    }

    async renderBoard(boardData) {
        // Clear all squares
        document.querySelectorAll('.square').forEach(square => {
            square.innerHTML = '';
            square.classList.remove('in-check');
        });

        // Place pieces
        const cells = boardData.cells;
        for (let rank = 0; rank < 8; rank++) {
            for (let file = 0; file < 8; file++) {
                const piece = cells[rank][file];
                if (piece) {
                    // Backend: rank=0 is a1 (bottom), rank=7 is a8 (top)
                    // Frontend: HTML has a8 at top, a1 at bottom
                    // So rank=0 should map to a1, rank=7 should map to a8
                    const displayRank = rank + 1; // Convert backend rank (0-7) to frontend rank (1-8)
                    const displayFile = String.fromCharCode(97 + file);
                    const squareName = displayFile + displayRank;

                    const squareElement = document.querySelector(`[data-square="${squareName}"]`);
                    if (squareElement) {
                        const pieceKey = `${piece.color}_${piece.type}`;
                        const iconUrl = PIECE_ICONS[pieceKey];

                        if (iconUrl) {
                            const img = document.createElement('img');
                            img.src = iconUrl;
                            img.alt = `${piece.color} ${piece.type}`;
                            img.dataset.pieceColor = piece.color;
                            img.dataset.pieceType = piece.type;
                            img.loading = 'lazy';

                            squareElement.appendChild(img);
                        }

                        // Highlight king in check
                        if (piece.type === 'KING') {
                            if ((piece.color === 'WHITE' && this.gameState.whiteInCheck) ||
                                (piece.color === 'BLACK' && this.gameState.blackInCheck)) {
                                squareElement.classList.add('in-check');
                            }
                        }
                    }
                }
            }
        }
    }

    async loadBoard() {
        try {
            this.setLoading(true);
            const boardData = await ChessAPI.getBoard();
            this.gameState.updateFromBoardData(boardData);
            await this.renderBoard(boardData);
            await this.updateMovesList();
            this.updateCurrentPlayer();
        } catch (error) {
            console.error('Error loading board:', error);
            this.showError('Ошибка при загрузке доски');
        } finally {
            this.setLoading(false);
        }
    }

    async updateMovesList() {
        try {
            const moves = await ChessAPI.getMoveList();
            this.gameState.moves = moves;
            this.renderMovesList(moves);
        } catch (error) {
            console.error('Error updating moves list:', error);
        }
    }

    renderMovesList(moves) {
        this.movesListElement.innerHTML = '';

        for (let i = 0; i < moves.length; i += 2) {
            const moveRow = document.createElement('div');
            moveRow.className = 'move-row';

            const moveNumber = document.createElement('div');
            moveNumber.className = 'move-number';
            moveNumber.textContent = Math.floor(i / 2) + 1;

            const moveWhite = document.createElement('div');
            moveWhite.className = 'move-white';
            moveWhite.textContent = moves[i] || '';

            const moveBlack = document.createElement('div');
            moveBlack.className = 'move-black';
            moveBlack.textContent = moves[i + 1] || '';

            moveRow.appendChild(moveNumber);
            moveRow.appendChild(moveWhite);
            moveRow.appendChild(moveBlack);

            this.movesListElement.appendChild(moveRow);
        }

        // Scroll to bottom
        this.movesListElement.scrollTop = this.movesListElement.scrollHeight;
    }

    updateCurrentPlayer() {
        const playerColor = this.gameState.sideToMove === 'WHITE' ? 'white' : 'black';
        const playerText = this.gameState.sideToMove === 'WHITE' ? 'Ход белых' : 'Ход черных';

        this.currentPlayerElement.innerHTML = `
            <div class="player-color ${playerColor}"></div>
            <span>${playerText}</span>
        `;
    }

    flipBoard() {
        this.gameState.isBoardFlipped = !this.gameState.isBoardFlipped;
        this.boardWrapper.classList.toggle('flipped', this.gameState.isBoardFlipped);
    }

    showNewGameModal() {
        document.getElementById('confirm-modal').classList.add('show');
    }

    hideNewGameModal() {
        document.getElementById('confirm-modal').classList.remove('show');
    }

    async startNewGame() {
        try {
            this.setLoading(true);
            await ChessAPI.resetGame();
            this.gameState.reset();
            await this.loadBoard();
            this.hideNewGameModal();
            this.showSuccess('Новая игра начата!');
        } catch (error) {
            console.error('Error starting new game:', error);
            this.showError('Ошибка при создании новой игры');
        } finally {
            this.setLoading(false);
        }
    }

    setLoading(loading) {
        document.body.classList.toggle('loading', loading);
    }

    showError(message) {
        console.error(message);
        alert(message);
    }

    showSuccess(message) {
        console.log(message);
    }
}

// Initialize the game when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    console.log('Initializing MiniChess...');

    const chessBoard = new ChessBoard();
    chessBoard.loadBoard();

    window.chessBoard = chessBoard;

    console.log('MiniChess initialized successfully!');
});
